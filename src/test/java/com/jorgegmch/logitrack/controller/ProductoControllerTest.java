package com.jorgegmch.logitrack.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.jorgegmch.logitrack.entity.Producto;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.security.JwtService;
import com.jorgegmch.logitrack.service.ProductoService;
import com.jorgegmch.logitrack.service.StockCalculadoService;
import com.jorgegmch.logitrack.service.UsuarioService;

/**
 * Test de integración (slice web) enfocado únicamente en el endpoint
 * nuevo GET /productos/{id}/stock. ProductoController ya existía sin
 * tests para sus otros métodos (listar, crear, actualizar, eliminar);
 * esos quedan fuera del alcance de este ciclo TDD puntual.
 */
@WebMvcTest(controllers = ProductoController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({ SecurityConfig.class, PasswordEncoderConfig.class })
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private StockCalculadoService stockCalculadoService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void obtenerStock_productoExistente_retorna200ConStockTotal() throws Exception {
        Producto producto = new Producto();
        producto.setIdProducto(3L);
        producto.setNombre("Silla ergonomica");

        when(productoService.buscarProductoPorId(3L)).thenReturn(producto);
        when(stockCalculadoService.calcularStockTotalProducto(3L)).thenReturn(15L);

        mockMvc.perform(get("/productos/3/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(3))
                .andExpect(jsonPath("$.stockTotal").value(15));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void obtenerStock_productoNoExistente_retorna404() throws Exception {
        when(productoService.buscarProductoPorId(anyLong()))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado con id: 99"));

        mockMvc.perform(get("/productos/99/stock"))
                .andExpect(status().isNotFound());
    }
}