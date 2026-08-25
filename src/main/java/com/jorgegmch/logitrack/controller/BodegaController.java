package com.jorgegmch.logitrack.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.dto.BodegaRequest;
import com.jorgegmch.logitrack.entity.Bodega;
import com.jorgegmch.logitrack.service.BodegaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/bodegas")
@Tag(name = "Bodegas", description = "Gestión de bodegas")
public class BodegaController {
    private final BodegaService bodegaService;

    public BodegaController(BodegaService bodegaService) {
        this.bodegaService = bodegaService;
    }

    @Operation(summary = "Listar todas las bodegas")
    @GetMapping
    public List<Bodega> listar() {
        return bodegaService.listarBodegas();
    }

    @Operation(summary = "Buscar una bodega por su id")
    @ApiResponse(responseCode = "200", description = "Bodega encontrada")
    @ApiResponse(responseCode = "404", description = "Bodega no encontrada")
    @GetMapping("/{id}")
    public Bodega buscarPorId(@PathVariable("id") Long id) {
        return bodegaService.buscarBodegaPorId(id);
    }

    @Operation(summary = "Crear una nueva bodega")
    @ApiResponse(responseCode = "201", description = "Bodega creada exitosamente")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Bodega crear(@Valid @RequestBody BodegaRequest request) {
        return bodegaService.crearBodega(request.getNombre(), request.getUbicacion(),
                request.getCapacidad(), request.getEncargadoId());
    }

    @Operation(summary = "Actualizar una bodega existente")
    @PutMapping("/{id}")
    public Bodega actualizar(@PathVariable("id") Long id, @Valid @RequestBody BodegaRequest request) {
        return bodegaService.actualizarBodega(id, request.getNombre(), request.getUbicacion(),
                request.getCapacidad(), request.getEncargadoId());
    }

    @Operation(summary = "Eliminar una bodega")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable("id") Long id) {
        bodegaService.eliminarBodega(id);
    }
}
