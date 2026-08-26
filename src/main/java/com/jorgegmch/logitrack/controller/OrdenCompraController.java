package com.jorgegmch.logitrack.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.dto.OrdenCompraRequest;
import com.jorgegmch.logitrack.entity.OrdenCompra;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.service.OrdenCompraService;
import com.jorgegmch.logitrack.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/ordenes")
@Tag(name = "Órdenes de compra", description = "Gestión de órdenes de compra de LogiTrack IQ")
public class OrdenCompraController {
    private final OrdenCompraService ordenCompraService;
    private final UsuarioService usuarioService;

    public OrdenCompraController(OrdenCompraService ordenCompraService, UsuarioService usuarioService) {
        this.ordenCompraService = ordenCompraService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Crear una orden de compra en estado BORRADOR. El usuario responsable siempre es quien esta autenticado.")
    @ApiResponse(responseCode = "201", description = "Orden creada exitosamente")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public OrdenCompra crear(@Valid @RequestBody OrdenCompraRequest request) {
        Long usuarioAutenticadoId = obtenerIdUsuarioAutenticado();

        return ordenCompraService.crearOrden(request.getProductoId(), request.getProveedorId(),
                request.getBodegaDestinoId(), request.getCantidad(), request.getPrecioUnitario(),
                usuarioAutenticadoId);
    }

    private Long obtenerIdUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
        return usuario.getIdUsuario();
    }
}