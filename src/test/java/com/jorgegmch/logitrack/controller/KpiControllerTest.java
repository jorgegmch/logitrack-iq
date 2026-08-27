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
import com.jorgegmch.logitrack.dto.ProductoRiesgoDTO;
import com.jorgegmch.logitrack.security.JwtService;
import com.jorgegmch.logitrack.service.KpiService;
import com.jorgegmch.logitrack.service.UsuarioService;

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

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void listarProductosEnRiesgo_retorna200ConLista() throws Exception {
        ProductoRiesgoDTO riesgo = new ProductoRiesgoDTO(1L, "Silla ergonomica", 1L, 10L,
                BigDecimal.valueOf(2.5), BigDecimal.valueOf(20), BigDecimal.valueOf(4), "CON_CONSUMO", 2L);

        when(kpiService.listarProductosEnRiesgo()).thenReturn(List.of(riesgo));

        mockMvc.perform(get("/kpis/riesgo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreProducto").value("Silla ergonomica"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void listarBodegasCriticas_retorna200ConLista() throws Exception {
        OcupacionBodegaDTO critica = new OcupacionBodegaDTO(1L, "Bodega Norte", BigDecimal.valueOf(92));

        when(kpiService.listarBodegasCriticas()).thenReturn(List.of(critica));

        mockMvc.perform(get("/kpis/bodegas-criticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Bodega Norte"));
    }
}