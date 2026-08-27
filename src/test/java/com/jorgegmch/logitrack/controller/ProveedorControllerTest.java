package com.jorgegmch.logitrack.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.jorgegmch.logitrack.entity.Proveedor;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.security.JwtService;
import com.jorgegmch.logitrack.service.ProveedorService;
import com.jorgegmch.logitrack.service.UsuarioService;

/**
 * Test de integración (slice web) de ProveedorController.
 *
 * Usa @Import(SecurityConfig.class, PasswordEncoderConfig.class) por
 * consistencia con OrdenCompraControllerTest, aunque este controlador
 * no tiene reglas de rol específicas (solo requiere autenticación).
 */
@WebMvcTest(controllers = ProveedorController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({ SecurityConfig.class, PasswordEncoderConfig.class })
class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProveedorService proveedorService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void listar_retornaListaDeProveedores() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Proveedor Uno");
        proveedor.setContacto("contacto@proveedor.com");
        proveedor.setDiasEntrega(5);

        when(proveedorService.listarProveedores()).thenReturn(List.of(proveedor));

        mockMvc.perform(get("/proveedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProveedor").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Proveedor Uno"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void buscarPorId_proveedorExistente_retorna200() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Proveedor Uno");
        proveedor.setContacto("contacto@proveedor.com");
        proveedor.setDiasEntrega(5);

        when(proveedorService.buscarProveedorPorId(1L)).thenReturn(proveedor);

        mockMvc.perform(get("/proveedores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProveedor").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void buscarPorId_proveedorNoExistente_retorna404() throws Exception {
        when(proveedorService.buscarProveedorPorId(anyLong()))
                .thenThrow(new RecursoNoEncontradoException("Proveedor no encontrado con id: 99"));

        mockMvc.perform(get("/proveedores/99"))
                .andExpect(status().isNotFound());
    }
}