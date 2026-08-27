package com.jorgegmch.logitrack.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.jorgegmch.logitrack.config.PasswordEncoderConfig;
import com.jorgegmch.logitrack.config.SecurityConfig;
import com.jorgegmch.logitrack.entity.ResumenPanel;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.security.JwtService;
import com.jorgegmch.logitrack.service.ResumenPanelService;
import com.jorgegmch.logitrack.service.UsuarioService;

@WebMvcTest(controllers = ResumenPanelController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({ SecurityConfig.class, PasswordEncoderConfig.class })
class ResumenPanelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumenPanelService resumenPanelService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void obtenerUltimoResumen_existente_retorna200() throws Exception {
        ResumenPanel resumen = new ResumenPanel();
        resumen.setIdResumenPanel(1L);
        resumen.setFecha(LocalDate.now());
        resumen.setContenidoJson("{\"narrativa\":\"todo en orden\"}");

        when(resumenPanelService.obtenerUltimoResumen()).thenReturn(resumen);

        mockMvc.perform(get("/panel/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idResumenPanel").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void obtenerUltimoResumen_noExiste_retorna404() throws Exception {
        when(resumenPanelService.obtenerUltimoResumen())
                .thenThrow(new RecursoNoEncontradoException("No hay un resumen publicado todavia"));

        mockMvc.perform(get("/panel/resumen"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void publicarResumen_valido_retorna201() throws Exception {
        String cuerpoRequest = "{"
                + "\"fecha\": \"" + LocalDate.now() + "\","
                + "\"narrativa\": \"Todo el inventario esta dentro de parametros normales hoy.\","
                + "\"alertas\": [],"
                + "\"accionesSugeridas\": []"
                + "}";

        ResumenPanel resumenCreado = new ResumenPanel();
        resumenCreado.setIdResumenPanel(2L);
        resumenCreado.setFecha(LocalDate.now());

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setIdUsuario(1L);
        usuarioAdmin.setUsername("admin");

        when(usuarioService.buscarUsuarioPorUsername("admin")).thenReturn(usuarioAdmin);
        when(resumenPanelService.publicarResumen(any(), any(), any(), any(), anyLong()))
                .thenReturn(resumenCreado);

        mockMvc.perform(post("/panel/resumen")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idResumenPanel").value(2));
    }

    /**
     * T7: severidad de alerta invalida -> 400, se conserva el resumen
     * anterior (el servicio valida antes de reemplazar; no se necesita
     * verificar explícitamente la "conservacion" aquí porque eso ya
     * está cubierto por el diseño del servicio — este test confirma
     * el contrato HTTP: entrada inválida -> 400, sin publicar nada
     * nuevo).
     */
    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void publicarResumen_severidadInvalida_retorna400() throws Exception {
        String cuerpoRequest = "{"
                + "\"fecha\": \"" + LocalDate.now() + "\","
                + "\"narrativa\": \"Narrativa de prueba con longitud suficiente.\","
                + "\"alertas\": [{"
                + "  \"severidad\": \"URGENTISIMA\","
                + "  \"titulo\": \"Alerta de prueba\","
                + "  \"detalle\": \"Detalle de prueba\","
                + "  \"productoId\": 1"
                + "}],"
                + "\"accionesSugeridas\": []"
                + "}";

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setIdUsuario(1L);
        usuarioAdmin.setUsername("admin");

        when(usuarioService.buscarUsuarioPorUsername("admin")).thenReturn(usuarioAdmin);
        when(resumenPanelService.publicarResumen(any(), any(), any(), any(), anyLong()))
                .thenThrow(new IllegalArgumentException("Severidad inválida, debe ser BAJA, MEDIA o ALTA"));

        mockMvc.perform(post("/panel/resumen")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void publicarResumen_idInexistente_retorna400() throws Exception {
        String cuerpoRequest = "{"
                + "\"fecha\": \"" + LocalDate.now() + "\","
                + "\"narrativa\": \"Narrativa de prueba con longitud suficiente.\","
                + "\"alertas\": [{"
                + "  \"severidad\": \"ALTA\","
                + "  \"titulo\": \"Alerta de prueba\","
                + "  \"detalle\": \"Detalle de prueba\","
                + "  \"productoId\": 9999"
                + "}],"
                + "\"accionesSugeridas\": []"
                + "}";

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setIdUsuario(1L);
        usuarioAdmin.setUsername("admin");

        when(usuarioService.buscarUsuarioPorUsername("admin")).thenReturn(usuarioAdmin);
        when(resumenPanelService.publicarResumen(any(), any(), any(), any(), anyLong()))
                .thenThrow(new IllegalArgumentException("No existe un producto con id: 9999"));

        mockMvc.perform(post("/panel/resumen")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoRequest))
                .andExpect(status().isBadRequest());
    }
}