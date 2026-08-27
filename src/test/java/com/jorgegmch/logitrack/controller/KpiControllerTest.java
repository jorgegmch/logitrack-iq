package com.jorgegmch.logitrack.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import com.jorgegmch.logitrack.dto.KpiResponse;
import com.jorgegmch.logitrack.dto.MovimientosAyerDTO;
import com.jorgegmch.logitrack.dto.OcupacionBodegaDTO;
import com.jorgegmch.logitrack.dto.OrdenesPorAprobarDTO;
import com.jorgegmch.logitrack.security.JwtService;
import com.jorgegmch.logitrack.service.KpiService;
import com.jorgegmch.logitrack.service.UsuarioService;

/**
 * Nota: los tests de listado detallado (riesgo, bodegas criticas) se
 * movieron a ProductoControllerTest y BodegaControllerTest, ya que
 * esos endpoints ahora viven exclusivamente ahi (rutas exactas del
 * PDF: /productos/riesgo, /bodegas/criticas). Este controlador solo
 * expone el resumen agregado.
 */
@WebMvcTest(controllers = KpiController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({ SecurityConfig.class, PasswordEncoderConfig.class })
class KpiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KpiService kpiService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void obtenerKpis_retorna200ConResumen() throws Exception {
        KpiResponse respuesta = new KpiResponse(
                ZonedDateTime.now(ZoneId.of("America/Bogota")),
                List.of(new OcupacionBodegaDTO(1L, "Bodega Central", BigDecimal.valueOf(75.5))),
                2L,
                1L,
                new OrdenesPorAprobarDTO(3L, BigDecimal.valueOf(150000)),
                new MovimientosAyerDTO(5L, 2L, 1L));

        when(kpiService.obtenerKpis()).thenReturn(respuesta);

        mockMvc.perform(get("/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productosEnQuiebre").value(2))
                .andExpect(jsonPath("$.productosEnRiesgo").value(1));
    }
}