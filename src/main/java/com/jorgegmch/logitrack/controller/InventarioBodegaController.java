package com.jorgegmch.logitrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.entity.InventarioBodega;
import com.jorgegmch.logitrack.service.InventarioBodegaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/inventario")
@Tag(name = "Inventario", description = "Consultas de stock por bodega")
public class InventarioBodegaController {
    private final InventarioBodegaService inventarioBodegaService;

    public InventarioBodegaController(InventarioBodegaService inventarioBodegaService) {
        this.inventarioBodegaService = inventarioBodegaService;
    }

    @Operation(summary = "Listar todo el inventario")
    @GetMapping
    public List<InventarioBodega> listar() {
        return inventarioBodegaService.listarInventario();
    }

    @Operation(summary = "Buscar un registro de inventario por su id")
    @ApiResponse(responseCode = "200", description = "Registro encontrado")
    @ApiResponse(responseCode = "404", description = "Registro no encontrado")
    @GetMapping("/{id}")
    public InventarioBodega buscarPorId(@PathVariable("id") Long id) {
        return inventarioBodegaService.buscarInventarioBodegaPorId(id);
    }

    @Operation(summary = "Listar productos con stock bajo (requisito 6)")
    @GetMapping("/stock-bajo")
    public List<InventarioBodega> stockBajo(@RequestParam(defaultValue = "10") Integer limite) {
        return inventarioBodegaService.listarStockBajoInventario(limite);
    }
}
