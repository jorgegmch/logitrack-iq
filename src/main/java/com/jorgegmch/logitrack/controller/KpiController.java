package com.jorgegmch.logitrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.dto.KpiResponse;
import com.jorgegmch.logitrack.dto.OcupacionBodegaDTO;
import com.jorgegmch.logitrack.dto.ProductoRiesgoDTO;
import com.jorgegmch.logitrack.service.KpiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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

    @Operation(summary = "Listar productos actualmente en riesgo de quiebre de stock")
    @GetMapping("/riesgo")
    public List<ProductoRiesgoDTO> listarProductosEnRiesgo() {
        return kpiService.listarProductosEnRiesgo();
    }

    @Operation(summary = "Listar bodegas con ocupacion critica (>= 90%)")
    @GetMapping("/bodegas-criticas")
    public List<OcupacionBodegaDTO> listarBodegasCriticas() {
        return kpiService.listarBodegasCriticas();
    }
}