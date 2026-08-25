package com.jorgegmch.logitrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.dto.ReporteResumenResponse;
import com.jorgegmch.logitrack.service.ReporteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/reportes")
@Tag(name = "Reportes", description = "Reportes y consultas agregadas del sistema")
public class ReporteController {
    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @Operation(summary = "Reporte de resumen general: stock total por bodega y productos más movidos")
    @GetMapping("/resumen")
    public ReporteResumenResponse obtenerResumenGeneral() {
        return reporteService.obtenerResumenGeneral();
    }
}
