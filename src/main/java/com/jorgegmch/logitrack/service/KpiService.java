package com.jorgegmch.logitrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jorgegmch.logitrack.dto.DesgloseStockBodegaDTO;
import com.jorgegmch.logitrack.dto.KpiResponse;
import com.jorgegmch.logitrack.dto.MovimientosAyerDTO;
import com.jorgegmch.logitrack.dto.OcupacionBodegaDTO;
import com.jorgegmch.logitrack.dto.OrdenesPorAprobarDTO;
import com.jorgegmch.logitrack.dto.ProductoRiesgoDTO;
import com.jorgegmch.logitrack.entity.Bodega;
import com.jorgegmch.logitrack.entity.OrdenCompra;
import com.jorgegmch.logitrack.entity.Producto;
import com.jorgegmch.logitrack.entity.enums.EstadoOrden;
import com.jorgegmch.logitrack.entity.enums.TipoMovimiento;
import com.jorgegmch.logitrack.repository.BodegaRepository;
import com.jorgegmch.logitrack.repository.MovimientoRepository;
import com.jorgegmch.logitrack.repository.OrdenCompraRepository;
import com.jorgegmch.logitrack.repository.ProductoRepository;

@Service
public class KpiService {
    private static final String ZONA_BOGOTA = "America/Bogota";
    private static final BigDecimal DIAS_CONSUMO = BigDecimal.valueOf(30);
    private static final BigDecimal FACTOR_SEGURIDAD = BigDecimal.valueOf(1.5);
    private static final BigDecimal PORCENTAJE_CRITICO = BigDecimal.valueOf(90);
    private static final int ESCALA = 4;

    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final MovimientoRepository movimientoRepository;
    private final StockCalculadoService stockCalculadoService;

    public KpiService(ProductoRepository productoRepository, BodegaRepository bodegaRepository,
            OrdenCompraRepository ordenCompraRepository, MovimientoRepository movimientoRepository,
            StockCalculadoService stockCalculadoService) {
        this.productoRepository = productoRepository;
        this.bodegaRepository = bodegaRepository;
        this.ordenCompraRepository = ordenCompraRepository;
        this.movimientoRepository = movimientoRepository;
        this.stockCalculadoService = stockCalculadoService;
    }

    public KpiResponse obtenerKpis() {
        List<OcupacionBodegaDTO> ocupacionPorBodega = calcularOcupacionPorBodega();
        Long productosEnQuiebre = contarProductosEnQuiebre();
        Long productosEnRiesgo = (long) listarProductosEnRiesgo().size();
        OrdenesPorAprobarDTO ordenesPorAprobar = calcularOrdenesPorAprobar();
        MovimientosAyerDTO movimientosAyer = calcularMovimientosAyer();

        ZonedDateTime calculadoEn = ZonedDateTime.now(ZoneId.of(ZONA_BOGOTA));

        return new KpiResponse(calculadoEn, ocupacionPorBodega, productosEnQuiebre, productosEnRiesgo,
                ordenesPorAprobar, movimientosAyer);
    }

    public List<ProductoRiesgoDTO> listarProductosEnRiesgo() {
        List<ProductoRiesgoDTO> resultado = new ArrayList<>();
        List<Producto> productos = productoRepository.findAll();

        for (Producto producto : productos) {
            if (producto.getProveedorPrincipalId() == null) {
                // R9: un producto sin proveedor principal no puede
                // aparecer como producto en riesgo.
                continue;
            }

            ProductoRiesgoDTO riesgo = evaluarRiesgo(producto);
            if (riesgo != null) {
                resultado.add(riesgo);
            }
        }

        return resultado;
    }

    public List<OcupacionBodegaDTO> listarBodegasCriticas() {
        List<OcupacionBodegaDTO> criticas = new ArrayList<>();
        List<OcupacionBodegaDTO> todas = calcularOcupacionPorBodega();

        for (OcupacionBodegaDTO bodega : todas) {
            if (bodega.getPorcentaje().compareTo(PORCENTAJE_CRITICO) >= 0) {
                criticas.add(bodega);
            }
        }

        return criticas;
    }

    private ProductoRiesgoDTO evaluarRiesgo(Producto producto) {
        Long stockTotal = stockCalculadoService.calcularStockTotalProducto(producto.getIdProducto());

        LocalDateTime hasta = LocalDateTime.now(ZoneId.of(ZONA_BOGOTA));
        LocalDateTime desde = LocalDate.now(ZoneId.of(ZONA_BOGOTA))
                .minusDays(29)
                .atStartOfDay();

        Long totalSalida = stockCalculadoService.calcularConsumoSalida(producto.getIdProducto(), desde, hasta);

        BigDecimal consumoDiarioPromedio = BigDecimal.valueOf(totalSalida)
                .divide(DIAS_CONSUMO, ESCALA, RoundingMode.HALF_UP);

        if (consumoDiarioPromedio.compareTo(BigDecimal.ZERO) == 0) {
            // R13: consumo 0 => cobertura null, estado SIN_CONSUMO.
            // Punto de reorden también es 0, por lo que nunca puede haber
            // riesgo (stock siempre >= 0), sin necesidad de caso especial.
            return null;
        }

        Integer diasEntrega = producto.getProveedorPrincipalId().getDiasEntrega();
        BigDecimal puntoReorden = consumoDiarioPromedio
                .multiply(BigDecimal.valueOf(diasEntrega))
                .multiply(FACTOR_SEGURIDAD);

        BigDecimal stockTotalDecimal = BigDecimal.valueOf(stockTotal);

        // R14: si el stock es igual al punto de reorden, no está en
        // riesgo; debe ser estrictamente menor.
        if (stockTotalDecimal.compareTo(puntoReorden) >= 0) {
            return null;
        }

        BigDecimal diasCobertura = stockTotalDecimal.divide(consumoDiarioPromedio, ESCALA, RoundingMode.HALF_UP);

        Long bodegaDestinoSugerida = sugerirBodegaDestino(producto.getIdProducto());

        return new ProductoRiesgoDTO(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getProveedorPrincipalId().getIdProveedor(),
                stockTotal,
                consumoDiarioPromedio,
                puntoReorden,
                diasCobertura,
                "CON_CONSUMO",
                bodegaDestinoSugerida);
    }

    private Long sugerirBodegaDestino(Long productoId) {
        List<DesgloseStockBodegaDTO> desglose = stockCalculadoService.calcularDesglosePorBodega(productoId);

        Long bodegaSugerida = null;
        Long menorStock = null;

        for (DesgloseStockBodegaDTO item : desglose) {
            if (menorStock == null
                    || item.getStock() < menorStock
                    || (item.getStock().equals(menorStock) && item.getBodegaId() < bodegaSugerida)) {
                menorStock = item.getStock();
                bodegaSugerida = item.getBodegaId();
            }
        }

        return bodegaSugerida;
    }

    private Long contarProductosEnQuiebre() {
        long contador = 0;
        List<Producto> productos = productoRepository.findAll();

        for (Producto producto : productos) {
            Long stockTotal = stockCalculadoService.calcularStockTotalProducto(producto.getIdProducto());
            if (stockTotal == 0) {
                contador = contador + 1;
            }
        }

        return contador;
    }

    private List<OcupacionBodegaDTO> calcularOcupacionPorBodega() {
        List<OcupacionBodegaDTO> resultado = new ArrayList<>();
        List<Bodega> bodegas = bodegaRepository.findAll();

        for (Bodega bodega : bodegas) {
            Long stockTotal = stockCalculadoService.calcularStockTotalBodega(bodega.getIdBodega());
            BigDecimal porcentaje = BigDecimal.valueOf(stockTotal)
                    .divide(BigDecimal.valueOf(bodega.getCapacidad()), ESCALA, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            resultado.add(new OcupacionBodegaDTO(bodega.getIdBodega(), bodega.getNombre(), porcentaje));
        }

        return resultado;
    }

    private OrdenesPorAprobarDTO calcularOrdenesPorAprobar() {
        List<OrdenCompra> borradores = ordenCompraRepository.findByEstado(EstadoOrden.BORRADOR);

        BigDecimal montoTotal = BigDecimal.ZERO;
        for (OrdenCompra orden : borradores) {
            montoTotal = montoTotal.add(orden.getTotal());
        }

        return new OrdenesPorAprobarDTO((long) borradores.size(), montoTotal);
    }

    private MovimientosAyerDTO calcularMovimientosAyer() {
        LocalDate ayer = LocalDate.now(ZoneId.of(ZONA_BOGOTA)).minusDays(1);
        LocalDateTime desde = ayer.atStartOfDay();
        LocalDateTime hasta = ayer.atTime(LocalTime.MAX);

        long entrada = movimientoRepository.countByTipoAndFechaBetween(TipoMovimiento.ENTRADA, desde, hasta);
        long salida = movimientoRepository.countByTipoAndFechaBetween(TipoMovimiento.SALIDA, desde, hasta);
        long transferencia = movimientoRepository.countByTipoAndFechaBetween(TipoMovimiento.TRANSFERENCIA, desde,
                hasta);

        return new MovimientosAyerDTO(entrada, salida, transferencia);
    }
}