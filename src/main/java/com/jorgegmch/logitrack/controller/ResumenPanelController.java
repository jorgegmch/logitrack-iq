package com.jorgegmch.logitrack.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.dto.ResumenPanelRequest;
import com.jorgegmch.logitrack.entity.ResumenPanel;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.service.ResumenPanelService;
import com.jorgegmch.logitrack.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/panel/resumen")
@Tag(name = "Panel de resumen", description = "Resumen diario de la torre de control de LogiTrack IQ")
public class ResumenPanelController {
    private final ResumenPanelService resumenPanelService;
    private final UsuarioService usuarioService;

    public ResumenPanelController(ResumenPanelService resumenPanelService, UsuarioService usuarioService) {
        this.resumenPanelService = resumenPanelService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Obtener el ultimo resumen publicado")
    @ApiResponse(responseCode = "200", description = "Resumen encontrado")
    @ApiResponse(responseCode = "404", description = "No hay ningun resumen publicado todavia")
    @GetMapping
    public ResumenPanel obtenerUltimoResumen() {
        return resumenPanelService.obtenerUltimoResumen();
    }

    @Operation(summary = "Publicar el resumen del dia. Reemplaza el resumen existente para la misma fecha.")
    @ApiResponse(responseCode = "201", description = "Resumen publicado exitosamente")
    @ApiResponse(responseCode = "400", description = "Contrato invalido (fecha, narrativa, alertas o acciones sugeridas)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResumenPanel publicarResumen(@Valid @RequestBody ResumenPanelRequest request) {
        Long usuarioAutenticadoId = obtenerIdUsuarioAutenticado();

        return resumenPanelService.publicarResumen(request.getFecha(), request.getNarrativa(),
                request.getAlertas(), request.getAccionesSugeridas(), usuarioAutenticadoId);
    }

    private Long obtenerIdUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
        return usuario.getIdUsuario();
    }
}