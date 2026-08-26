package com.jorgegmch.logitrack.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.jorgegmch.logitrack.entity.OrdenCompra;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.entity.enums.EstadoOrden;
import com.jorgegmch.logitrack.security.JwtService;
import com.jorgegmch.logitrack.service.OrdenCompraService;
import com.jorgegmch.logitrack.service.UsuarioService;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * Test de integración (slice web) de OrdenCompraController.
 *
 * Estado esperado en este momento del proceso TDD: OrdenCompraController
 * existe como esqueleto SIN el método POST /ordenes todavía. Este test
 * debe fallar con 404 Not Found (rojo real: la ruta no está mapeada),
 * no con un error de compilación. Cuando se agregue el método
 * @PostMapping correspondiente, el mismo test —sin modificarse— debe
 * pasar a verde (201 Created).
 *
 * Nota 1: usa @MockitoBean (Spring Framework 6.2+/7), no @MockBean
 * (org.springframework.boot.test.mock.mockito.MockBean), que fue
 * removido en Spring Boot 4.0 tras ser deprecado desde la 3.4.
 *
 * Nota 3: excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
 * evita que Spring Boot cree su propio usuario en memoria
 * (inMemoryUserDetailsManager) como fallback automático. Sin esto,
 * ese bean automático y nuestro @MockitoBean de UsuarioService (que
 * también implementa UserDetailsService) compiten como candidatos,
 * y JwtAuthenticationFilter no puede decidir cuál usar.
 */
@WebMvcTest(controllers = OrdenCompraController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
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

        when(ordenCompraService.crearOrden(anyLong(), anyLong(), anyLong(), any(), any(BigDecimal.class),
                anyLong())).thenReturn(ordenSimulada);

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setIdUsuario(1L);
        usuarioAdmin.setUsername("admin");

        when(usuarioService.buscarUsuarioPorUsername("admin")).thenReturn(usuarioAdmin);

        mockMvc.perform(post("/ordenes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOrdenCompra").value(1))
                .andExpect(jsonPath("$.estado").value("BORRADOR"));
    }
}