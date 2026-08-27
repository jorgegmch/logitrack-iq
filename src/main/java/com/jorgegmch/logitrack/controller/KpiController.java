package com.jorgegmch.logitrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.dto.KpiResponse;
import com.jorgegmch.logitrack.service.KpiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Nota: los listados detallados de productos en riesgo y bodegas
 * criticas viven en ProductoController (/productos/riesgo) y
 * BodegaController (/bodegas/criticas) respectivamente, siguiendo las
 * rutas exactas exigidas por el PDF de requerimientos. Este
 * controlador solo expone el resumen agregado del dashboard.
 */
@RestController
@RequestMapping("/kpis")
@Tag(name = "KPIs", description = "Indicadores de la torre de control de LogiTrack IQ")
public class KpiController {
    private final KpiService kpiService;

    public KpiController(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @Operation(summary = "Obtener el resumen completo de KPIs del dashboard")
    @GetMapping
    public KpiResponse obtenerKpis() {
        return kpiService.obtenerKpis();
    }
}