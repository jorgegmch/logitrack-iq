package com.jorgegmch.logitrack.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.dto.LoginRequest;
import com.jorgegmch.logitrack.dto.LoginResponse;
import com.jorgegmch.logitrack.dto.UsuarioRegisterRequest;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.security.JwtService;
import com.jorgegmch.logitrack.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Login y registro de usuarios")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UsuarioService usuarioService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Iniciar sesión y obtener un token JWT")
    @ApiResponse(responseCode = "200", description = "Login exitoso")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception e) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        Usuario usuario = usuarioService.buscarUsuarioPorUsername(request.getUsername());
        String token = jwtService.generarToken(usuario.getUsername(), usuario.getRol().name());

        return new LoginResponse(usuario.getIdUsuario(), token, usuario.getUsername(), usuario.getRol().name());
    }

    @Operation(summary = "Registrar un nuevo usuario")
    @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public Usuario register(@Valid @RequestBody UsuarioRegisterRequest request) {
        return usuarioService.registrarUsuario(request.getUsername(), request.getPassword(), request.getRol());
    }
}
