package com.jorgegmch.logitrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.entity.Auditoria;
import com.jorgegmch.logitrack.entity.enums.TipoOperacion;
import com.jorgegmch.logitrack.service.AuditoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auditorias")
@Tag(name = "Auditorías", description = "Consulta de registros de auditoría")
public class AuditoriaController {
    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Operation(summary = "Listar todas las auditorías")
    @GetMapping
    public List<Auditoria> listar() {
        return auditoriaService.listarAuditorias();
    }

    @Operation(summary = "Buscar auditorías por usuario responsable")
    @GetMapping("/usuario/{usuarioId}")
    public List<Auditoria> buscarPorUsuario(@PathVariable("usuarioId") Long usuarioId) {
        return auditoriaService.buscarPorUsuario(usuarioId);
    }

    @Operation(summary = "Buscar auditorías por tipo de operación")
    @GetMapping("/tipo/{tipoOperacion}")
    public List<Auditoria> buscarPorTipoOperacion(@PathVariable("tipoOperacion") TipoOperacion tipoOperacion) {
        return auditoriaService.buscarPorTipoOperacion(tipoOperacion);
    }
}