package com.jorgegmch.logitrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar todos los usuarios")
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listarUsuarios();
    }

    @Operation(summary = "Buscar un usuario por su id")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable("id") Long id) {
        return usuarioService.buscarUsuarioPorId(id);
    }

    @Operation(summary = "Desactivar un usuario")
    @PatchMapping("/{id}/desactivar")
    public Usuario desactivar(@PathVariable("id") Long id) {
        return usuarioService.desactivarUsuario(id);
    }

    @Operation(summary = "Reactivar un usuario")
    @PatchMapping("/{id}/reactivar")
    public Usuario reactivar(@PathVariable("id") Long id) {
        return usuarioService.reactivarUsuario(id);
    }
}