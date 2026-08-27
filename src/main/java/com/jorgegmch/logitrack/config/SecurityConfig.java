package com.jorgegmch.logitrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.jorgegmch.logitrack.security.JwtAuthenticationFilter;
import com.jorgegmch.logitrack.service.UsuarioService;

@Configuration
public class SecurityConfig {

    private final UsuarioService usuarioService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UsuarioService usuarioService, JwtAuthenticationFilter jwtAuthenticationFilter,
            PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/html/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/register").hasRole("ADMIN")
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        .requestMatchers(HttpMethod.POST, "/productos/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/productos/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/productos/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/bodegas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/bodegas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/bodegas/**").hasRole("ADMIN")

                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/auditorias/**").hasRole("ADMIN")

                        // Corrección: AGENTE no debe poder registrar movimientos
                        // manualmente (tabla de permisos, sección 7 de la
                        // especificación). Se agrega ANTES del catch-all
                        // genérico de /movimientos/**.
                        .requestMatchers(HttpMethod.POST, "/movimientos/**").hasAnyRole("ADMIN", "EMPLEADO")

                        // Reglas nuevas de LogiTrack IQ (03-diseno.md, sección 7)
                        .requestMatchers(HttpMethod.POST, "/ordenes").hasAnyRole("ADMIN", "AGENTE")
                        .requestMatchers(HttpMethod.PATCH, "/ordenes/*/estado").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/ordenes/*/pdf").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/panel/resumen").hasAnyRole("ADMIN", "AGENTE")

                        .requestMatchers("/bodegas/**", "/productos/**", "/movimientos/**",
                                "/inventario/**", "/reportes/**", "/api/reportes/**").authenticated()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}