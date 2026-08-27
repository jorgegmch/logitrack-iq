package com.jorgegmch.logitrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.entity.Proveedor;
import com.jorgegmch.logitrack.service.ProveedorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/proveedores")
@Tag(name = "Proveedores", description = "Consulta de proveedores de LogiTrack IQ")
public class ProveedorController {
    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @Operation(summary = "Listar todos los proveedores")
    @GetMapping
    public List<Proveedor> listar() {
        return proveedorService.listarProveedores();
    }

    @Operation(summary = "Buscar un proveedor por su id")
    @ApiResponse(responseCode = "200", description = "Proveedor encontrado")
    @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    @GetMapping("/{id}")
    public Proveedor buscarPorId(@PathVariable("id") Long id) {
        return proveedorService.buscarProveedorPorId(id);
    }
}