package com.jorgegmch.logitrack.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.jorgegmch.logitrack.config.PasswordEncoderConfig;
import com.jorgegmch.logitrack.config.SecurityConfig;
import com.jorgegmch.logitrack.dto.OcupacionBodegaDTO;
import com.jorgegmch.logitrack.security.JwtService;
import com.jorgegmch.logitrack.service.BodegaService;
import com.jorgegmch.logitrack.service.KpiService;
import com.jorgegmch.logitrack.service.UsuarioService;

/**
 * Test de integración (slice web) enfocado en GET /bodegas/criticas,
 * ruta exigida textualmente por el PDF de requerimientos (distinta de
 * /kpis/bodegas-criticas que se habia usado antes). BodegaController
 * ya existia sin tests previos; los demas metodos (listar, crear,
 * actualizar, eliminar, buscarPorId) quedan fuera de este ciclo.
 */
@WebMvcTest(controllers = BodegaController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({ SecurityConfig.class, PasswordEncoderConfig.class })
class BodegaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BodegaService bodegaService;

    @MockitoBean
    private KpiService kpiService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void listarBodegasCriticas_retorna200ConLista() throws Exception {
        OcupacionBodegaDTO critica = new OcupacionBodegaDTO(1L, "Bodega Norte", BigDecimal.valueOf(92));

        when(kpiService.listarBodegasCriticas()).thenReturn(List.of(critica));

        mockMvc.perform(get("/bodegas/criticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Bodega Norte"));
    }
}