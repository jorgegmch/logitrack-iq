package com.jorgegmch.logitrack.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.entity.Auditoria;
import com.jorgegmch.logitrack.entity.Movimiento;
import com.jorgegmch.logitrack.entity.enums.TipoMovimiento;
import com.jorgegmch.logitrack.service.ReporteAvanzadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes Avanzados", description = "Consultas consolidadas de movimientos y auditoria con filtros combinables")
public class ReporteAvanzadoController {
    private final ReporteAvanzadoService reporteAvanzadoService;

    public ReporteAvanzadoController(ReporteAvanzadoService reporteAvanzadoService) {
        this.reporteAvanzadoService = reporteAvanzadoService;
    }

    @Operation(summary = "Consultar movimientos filtrados por bodega, producto, tipo de movimiento y/o rango de fechas")
    @GetMapping("/movimientos")
    public List<Movimiento> buscarMovimientos(
            @RequestParam(name = "bodega", required = false) Long bodegaId,
            @RequestParam(name = "producto", required = false) Long productoId,
            @RequestParam(name = "tipoMovimiento", required = false) TipoMovimiento tipoMovimiento,
            @RequestParam(name = "fechaInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(name = "fechaFin", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        return reporteAvanzadoService.buscarMovimientos(bodegaId, productoId, tipoMovimiento, fechaInicio, fechaFin);
    }

    @Operation(summary = "Consultar auditorias filtradas por producto, rango de fechas y/o campo modificado")
    @GetMapping("/auditoria")
    public List<Auditoria> buscarAuditorias(
            @RequestParam(name = "producto", required = false) Long productoId,
            @RequestParam(name = "fechaInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(name = "fechaFin", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(name = "campoModificado", required = false) String campoModificado) {

        return reporteAvanzadoService.buscarAuditorias(productoId, fechaInicio, fechaFin, campoModificado);
    }
}