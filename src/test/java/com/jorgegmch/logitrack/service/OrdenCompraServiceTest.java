package com.jorgegmch.logitrack.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jorgegmch.logitrack.entity.Bodega;
import com.jorgegmch.logitrack.entity.OrdenCompra;
import com.jorgegmch.logitrack.entity.Producto;
import com.jorgegmch.logitrack.entity.enums.EstadoOrden;
import com.jorgegmch.logitrack.entity.enums.TipoMovimiento;
import com.jorgegmch.logitrack.repository.BodegaRepository;
import com.jorgegmch.logitrack.repository.OrdenCompraRepository;
import com.jorgegmch.logitrack.repository.ProductoRepository;
import com.jorgegmch.logitrack.repository.ProveedorRepository;
import com.jorgegmch.logitrack.repository.UsuarioRepository;

/**
 * Test unitario puro de OrdenCompraService (sin contexto de Spring):
 * cubre T3 (cantidad <=0), T4 (CANCELADA no aprobable) y T5
 * (APROBADA->RECIBIDA genera movimiento ENTRADA).
 *
 * Recreado tras confirmar que el archivo original nunca se copio al
 * proyecto (ver evidencia-sdd.md, seccion "pendientes tecnicos").
 */
@ExtendWith(MockitoExtension.class)
class OrdenCompraServiceTest {

    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private BodegaRepository bodegaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MovimientoService movimientoService;

    @Mock
    private PdfService pdfService;

    private OrdenCompraService ordenCompraService;

    @BeforeEach
    void configurar() {
        ordenCompraService = new OrdenCompraService(ordenCompraRepository, productoRepository,
                proveedorRepository, bodegaRepository, usuarioRepository, movimientoService, pdfService);
    }

    /**
     * T3: la cantidad debe ser mayor a cero al crear una orden.
     */
    @Test
    void crearOrden_cantidadCero_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> ordenCompraService.crearOrden(1L, 1L, 1L, 0, BigDecimal.TEN, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    void crearOrden_cantidadNegativa_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> ordenCompraService.crearOrden(1L, 1L, 1L, -5, BigDecimal.TEN, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }

    /**
     * T4: una orden CANCELADA no puede pasar a ningun otro estado. No
     * debe registrarse ningun movimiento (never()).
     */
    @Test
    void cambiarEstado_ordenCancelada_noEsAprobableYNoRegistraMovimiento() {
        OrdenCompra ordenCancelada = new OrdenCompra();
        ordenCancelada.setIdOrdenCompra(1L);
        ordenCancelada.setEstado(EstadoOrden.CANCELADA);

        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(ordenCancelada));

        assertThatThrownBy(() -> ordenCompraService.cambiarEstado(1L, EstadoOrden.APROBADA, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transición no permitida");

        verify(movimientoService, never()).registrarMovimiento(
                any(), any(), any(), any(), anyList(), anyList());
        verify(ordenCompraRepository, never()).save(any());
    }

    /**
     * T5: al pasar de APROBADA a RECIBIDA, se debe registrar un
     * movimiento de tipo ENTRADA con el producto y cantidad exactos de
     * la orden, hacia la bodega destino de la orden.
     */
    @Test
    void cambiarEstado_aprobadaARecibida_generaMovimientoEntradaConParametrosExactos() {
        Producto producto = new Producto();
        producto.setIdProducto(7L);

        Bodega bodegaDestino = new Bodega();
        bodegaDestino.setIdBodega(3L);

        OrdenCompra ordenAprobada = new OrdenCompra();
        ordenAprobada.setIdOrdenCompra(1L);
        ordenAprobada.setEstado(EstadoOrden.APROBADA);
        ordenAprobada.setProductoId(producto);
        ordenAprobada.setBodegaDestinoId(bodegaDestino);
        ordenAprobada.setCantidad(15);

        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(ordenAprobada));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.cambiarEstado(1L, EstadoOrden.RECIBIDA, 99L);

        verify(movimientoService, times(1)).registrarMovimiento(
                eq(TipoMovimiento.ENTRADA),
                eq(99L),
                eq(null),
                eq(3L),
                eq(List.of(7L)),
                eq(List.of(15)));

        assertThat(resultado.getEstado()).isEqualTo(EstadoOrden.RECIBIDA);
    }
}