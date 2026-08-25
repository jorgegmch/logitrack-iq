package com.jorgegmch.logitrack.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.dto.MovimientoRequest;
import com.jorgegmch.logitrack.entity.Movimiento;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.service.MovimientoService;
import com.jorgegmch.logitrack.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/movimientos")
@Tag(name = "Movimientos", description = "Registro y consulta de movimientos de inventario")
public class MovimientoController {
    private final MovimientoService movimientoService;
    private final UsuarioService usuarioService;

    public MovimientoController(MovimientoService movimientoService, UsuarioService usuarioService) {
        this.movimientoService = movimientoService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar todos los movimientos")
    @GetMapping
    public List<Movimiento> listar() {
        return movimientoService.listarMovimientos();
    }

    @Operation(summary = "Buscar un movimiento por su id")
    @ApiResponse(responseCode = "200", description = "Movimiento encontrado")
    @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    @GetMapping("/{id}")
    public Movimiento buscarPorId(@PathVariable("id") Long id) {
        return movimientoService.buscarMovimientoPorId(id);
    }

    @Operation(summary = "Listar movimientos por rango de fechas")
    @GetMapping("/rango")
    public List<Movimiento> listarPorRango(
            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return movimientoService.listarMovimientosPorRango(desde, hasta);
    }

    @Operation(summary = "Registrar un nuevo movimiento (entrada, salida o transferencia). El usuario responsable siempre es quien esta autenticado.")
    @ApiResponse(responseCode = "201", description = "Movimiento registrado exitosamente")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Movimiento registrar(@Valid @RequestBody MovimientoRequest request) {
        Long usuarioAutenticadoId = obtenerIdUsuarioAutenticado();

        return movimientoService.registrarMovimiento(request.getTipo(), usuarioAutenticadoId,
                request.getBodegaOrigenId(), request.getBodegaDestinoId(),
                request.getProductoIds(), request.getCantidades());
    }

    private Long obtenerIdUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
        return usuario.getIdUsuario();
    }
}