package com.jorgegmch.logitrack.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
import com.jorgegmch.logitrack.entity.OrdenCompra;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.entity.enums.EstadoOrden;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.security.JwtService;
import com.jorgegmch.logitrack.service.OrdenCompraService;
import com.jorgegmch.logitrack.service.UsuarioService;

/**
 * Test de integración (slice web) de OrdenCompraController.
 *
 * Nota 1: usa @MockitoBean (Spring Framework 6.2+/7), no @MockBean, que
 * fue removido en Spring Boot 4.0 tras ser deprecado desde la 3.4.
 *
 * Nota 2: excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
 * evita que Spring Boot cree su propio usuario en memoria como fallback,
 * que competiría con el @MockitoBean de UsuarioService.
 *
 * Nota 3: @Import(SecurityConfig.class, PasswordEncoderConfig.class) es
 * OBLIGATORIO: @WebMvcTest NO escanea clases @Configuration genéricas,
 * así que sin este import el test corre con la cadena de seguridad POR
 * DEFECTO de Spring Boot (CSRF activo, sin reglas de roles) en vez de
 * la del proyecto — y las reglas de autorización nunca se evalúan.
 * PasswordEncoderConfig se importa porque SecurityConfig lo necesita
 * en su constructor.
 */
@WebMvcTest(controllers = OrdenCompraController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({ SecurityConfig.class, PasswordEncoderConfig.class })
class OrdenCompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdenCompraService ordenCompraService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void crear_ordenValida_retorna201YCuerpoEsperado() throws Exception {
        String cuerpoRequest = "{"
                + "\"productoId\": 1,"
                + "\"proveedorId\": 1,"
                + "\"bodegaDestinoId\": 1,"
                + "\"cantidad\": 20,"
                + "\"precioUnitario\": 45000.0"
                + "}";

        OrdenCompra ordenSimulada = new OrdenCompra();
        ordenSimulada.setIdOrdenCompra(1L);
        ordenSimulada.setEstado(EstadoOrden.BORRADOR);
        ordenSimulada.setCantidad(20);
        ordenSimulada.setPrecioUnitario(BigDecimal.valueOf(45000.0));
        ordenSimulada.setTotal(BigDecimal.valueOf(900000.0));
        ordenSimulada.setFechaCreacion(LocalDateTime.now());

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setIdUsuario(1L);
        usuarioAdmin.setUsername("admin");

        when(usuarioService.buscarUsuarioPorUsername("admin")).thenReturn(usuarioAdmin);
        when(ordenCompraService.crearOrden(anyLong(), anyLong(), anyLong(), any(), any(BigDecimal.class),
                anyLong())).thenReturn(ordenSimulada);

        mockMvc.perform(post("/ordenes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOrdenCompra").value(1))
                .andExpect(jsonPath("$.estado").value("BORRADOR"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void adminCambiaEstado_ordenValida_retorna200ConEstadoActualizado() throws Exception {
        String cuerpoRequest = "{\"estado\": \"APROBADA\"}";

        OrdenCompra ordenAprobada = new OrdenCompra();
        ordenAprobada.setIdOrdenCompra(5L);
        ordenAprobada.setEstado(EstadoOrden.APROBADA);

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setIdUsuario(1L);
        usuarioAdmin.setUsername("admin");

        when(usuarioService.buscarUsuarioPorUsername("admin")).thenReturn(usuarioAdmin);
        when(ordenCompraService.cambiarEstado(anyLong(), any(EstadoOrden.class), anyLong()))
                .thenReturn(ordenAprobada);

        mockMvc.perform(patch("/ordenes/5/estado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOrdenCompra").value(5))
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    /**
     * T6: AGENTE intenta aprobar una orden -> 403.
     *
     * No se necesita mockear ordenCompraService.cambiarEstado ni
     * usuarioService: si la regla de seguridad está bien configurada,
     * la petición nunca debe llegar al controlador ni a la capa de
     * servicio — Spring Security la rechaza antes.
     */
    @Test
    @WithMockUser(username = "agente_mcp", roles = { "AGENTE" })
    void agenteIntentaAprobarOrden_retorna403() throws Exception {
        String cuerpoRequest = "{\"estado\": \"APROBADA\"}";

        mockMvc.perform(patch("/ordenes/5/estado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void listar_retornaListaDeOrdenes() throws Exception {
        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrdenCompra(1L);
        orden.setEstado(EstadoOrden.BORRADOR);

        when(ordenCompraService.listarOrdenes(null)).thenReturn(List.of(orden));

        mockMvc.perform(get("/ordenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idOrdenCompra").value(1))
                .andExpect(jsonPath("$[0].estado").value("BORRADOR"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void buscarPorId_ordenExistente_retorna200() throws Exception {
        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrdenCompra(1L);
        orden.setEstado(EstadoOrden.BORRADOR);

        when(ordenCompraService.buscarOrdenPorId(1L)).thenReturn(orden);

        mockMvc.perform(get("/ordenes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOrdenCompra").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void buscarPorId_ordenNoExistente_retorna404() throws Exception {
        when(ordenCompraService.buscarOrdenPorId(anyLong()))
                .thenThrow(new RecursoNoEncontradoException("Orden de compra no encontrada con id: 99"));

        mockMvc.perform(get("/ordenes/99"))
                .andExpect(status().isNotFound());
    }
}