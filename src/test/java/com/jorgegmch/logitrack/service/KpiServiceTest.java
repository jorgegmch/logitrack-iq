package com.jorgegmch.logitrack.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jorgegmch.logitrack.dto.ProductoRiesgoDTO;
import com.jorgegmch.logitrack.entity.Producto;
import com.jorgegmch.logitrack.entity.Proveedor;
import com.jorgegmch.logitrack.repository.BodegaRepository;
import com.jorgegmch.logitrack.repository.MovimientoRepository;
import com.jorgegmch.logitrack.repository.OrdenCompraRepository;
import com.jorgegmch.logitrack.repository.ProductoRepository;

/**
 * Test unitario puro de KpiService (sin contexto de Spring): cubre T1
 * (consumo=0 -> lista vacia), T2 (stock==puntoReorden -> lista vacia,
 * limite exacto) y el caso positivo (stock < puntoReorden -> aparece
 * en la lista de riesgo).
 *
 * Recreado tras confirmar que el archivo original nunca se copio al
 * proyecto (ver evidencia-sdd.md, seccion "pendientes tecnicos").
 */
@ExtendWith(MockitoExtension.class)
class KpiServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private BodegaRepository bodegaRepository;

    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private StockCalculadoService stockCalculadoService;

    private KpiService kpiService;

    @BeforeEach
    void configurar() {
        kpiService = new KpiService(productoRepository, bodegaRepository, ordenCompraRepository,
                movimientoRepository, stockCalculadoService);
    }

    private Producto construirProductoConProveedor(Integer diasEntrega) {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Proveedor de prueba");
        proveedor.setDiasEntrega(diasEntrega);

        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setNombre("Producto de prueba");
        producto.setProveedorPrincipalId(proveedor);

        return producto;
    }

    /**
     * T1: si el consumo total de los ultimos 30 dias es 0, el producto
     * nunca puede estar en riesgo (consumoDiarioPromedio == 0 ->
     * estado SIN_CONSUMO, sin importar el stock).
     */
    @Test
    void listarProductosEnRiesgo_consumoCero_retornaListaVacia() {
        Producto producto = construirProductoConProveedor(5);

        when(productoRepository.findAll()).thenReturn(List.of(producto));
        when(stockCalculadoService.calcularStockTotalProducto(1L)).thenReturn(50L);
        when(stockCalculadoService.calcularConsumoSalida(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        List<ProductoRiesgoDTO> resultado = kpiService.listarProductosEnRiesgo();

        assertThat(resultado).isEmpty();
    }

    /**
     * T2: si el stock es exactamente igual al punto de reorden, el
     * producto NO esta en riesgo (debe ser estrictamente menor).
     *
     * Con consumo total = 60 en 30 dias -> consumoDiarioPromedio = 2.0.
     * diasEntrega = 5, factor de seguridad = 1.5 ->
     * puntoReorden = 2.0 * 5 * 1.5 = 15.0. Stock = 15 (exactamente
     * igual) -> no debe aparecer en la lista.
     */
    @Test
    void listarProductosEnRiesgo_stockIgualAlPuntoDeReorden_retornaListaVacia() {
        Producto producto = construirProductoConProveedor(5);

        when(productoRepository.findAll()).thenReturn(List.of(producto));
        when(stockCalculadoService.calcularStockTotalProducto(1L)).thenReturn(15L);
        when(stockCalculadoService.calcularConsumoSalida(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(60L);

        List<ProductoRiesgoDTO> resultado = kpiService.listarProductosEnRiesgo();

        assertThat(resultado).isEmpty();
    }

    /**
     * Caso positivo: mismo escenario que T2, pero con stock = 14
     * (estrictamente menor al punto de reorden de 15) -> el producto
     * SI debe aparecer en la lista de riesgo.
     */
    @Test
    void listarProductosEnRiesgo_stockMenorAlPuntoDeReorden_apareceEnLaLista() {
        Producto producto = construirProductoConProveedor(5);

        when(productoRepository.findAll()).thenReturn(List.of(producto));
        when(stockCalculadoService.calcularStockTotalProducto(1L)).thenReturn(14L);
        when(stockCalculadoService.calcularConsumoSalida(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(60L);
        when(stockCalculadoService.calcularDesglosePorBodega(1L)).thenReturn(List.of());

        List<ProductoRiesgoDTO> resultado = kpiService.listarProductosEnRiesgo();

        assertThat(resultado).hasSize(1);
        ProductoRiesgoDTO riesgo = resultado.get(0);
        assertThat(riesgo.getProductoId()).isEqualTo(1L);
        assertThat(riesgo.getStockTotal()).isEqualTo(14L);
        assertThat(riesgo.getPuntoReorden()).isEqualByComparingTo(BigDecimal.valueOf(15));
        assertThat(riesgo.getEstadoCobertura()).isEqualTo("CON_CONSUMO");
    }
}