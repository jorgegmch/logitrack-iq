package com.jorgegmch.logitrack.service;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.entity.enums.Rol;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())))
                .disabled(!usuario.getActivo())
                .build();
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarUsuarioPorUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El username no puede estar vacío");
        }
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            throw new RecursoNoEncontradoException("El usuario " + username + " no se encuentra registrado");
        }
        return usuario;
    }

    public Usuario buscarUsuarioPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser un número positivo");
        }
        Usuario usuarioId = usuarioRepository.findById(id).orElse(null);
        if (usuarioId == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + id);
        }
        return usuarioId;
    }

    @Transactional
    public Usuario registrarUsuario(String username, String password, Rol rol) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario debe tener un username");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario debe tener una contraseña");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El usuario debe tener un rol");
        }
        if (usuarioRepository.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese username");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username.trim());
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario desactivarUsuario(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        if (!usuario.getActivo()) {
            throw new IllegalArgumentException("El usuario ya se encuentra inactivo");
        }    
        usuario.setActivo(false);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario reactivarUsuario(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        if (usuario.getActivo()) {
            throw new IllegalArgumentException("El usuario ya se encuentra activo");
        }
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }
}