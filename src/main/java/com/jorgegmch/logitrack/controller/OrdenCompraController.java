package com.jorgegmch.logitrack.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.dto.CambiarEstadoRequest;
import com.jorgegmch.logitrack.dto.OrdenCompraRequest;
import com.jorgegmch.logitrack.entity.OrdenCompra;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.entity.enums.EstadoOrden;
import com.jorgegmch.logitrack.service.OrdenCompraService;
import com.jorgegmch.logitrack.service.UsuarioService;

import java.util.List;

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

    @Operation(summary = "Listar órdenes de compra, opcionalmente filtradas por estado")
    @GetMapping
    public List<OrdenCompra> listar(@RequestParam(name = "estado", required = false) EstadoOrden estado) {
        return ordenCompraService.listarOrdenes(estado);
    }

    @Operation(summary = "Buscar una orden de compra por su id")
    @ApiResponse(responseCode = "200", description = "Orden encontrada")
    @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    @GetMapping("/{id}")
    public OrdenCompra buscarPorId(@PathVariable("id") Long id) {
        return ordenCompraService.buscarOrdenPorId(id);
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

    @Operation(summary = "Cambiar el estado de una orden (aprobar, recibir o cancelar). Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Transición de estado no permitida")
    @ApiResponse(responseCode = "403", description = "Rol sin permisos para esta acción")
    @PatchMapping("/{id}/estado")
    public OrdenCompra cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody CambiarEstadoRequest request) {
        Long usuarioAutenticadoId = obtenerIdUsuarioAutenticado();

        return ordenCompraService.cambiarEstado(id, request.getEstado(), usuarioAutenticadoId);
    }

    private Long obtenerIdUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
        return usuario.getIdUsuario();
    }
}