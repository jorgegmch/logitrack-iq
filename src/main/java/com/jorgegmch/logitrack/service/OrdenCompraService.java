package com.jorgegmch.logitrack.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorgegmch.logitrack.entity.Bodega;
import com.jorgegmch.logitrack.entity.OrdenCompra;
import com.jorgegmch.logitrack.entity.Producto;
import com.jorgegmch.logitrack.entity.Proveedor;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.entity.enums.EstadoOrden;
import com.jorgegmch.logitrack.entity.enums.TipoMovimiento;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.repository.BodegaRepository;
import com.jorgegmch.logitrack.repository.OrdenCompraRepository;
import com.jorgegmch.logitrack.repository.ProductoRepository;
import com.jorgegmch.logitrack.repository.ProveedorRepository;
import com.jorgegmch.logitrack.repository.UsuarioRepository;

@Service
public class OrdenCompraService {
    private final OrdenCompraRepository ordenCompraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoService movimientoService;
    private final PdfService pdfService;

    public OrdenCompraService(OrdenCompraRepository ordenCompraRepository, ProductoRepository productoRepository,
            ProveedorRepository proveedorRepository, BodegaRepository bodegaRepository,
            UsuarioRepository usuarioRepository, MovimientoService movimientoService, PdfService pdfService) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.bodegaRepository = bodegaRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoService = movimientoService;
        this.pdfService = pdfService;
    }

    public List<OrdenCompra> listarOrdenes(EstadoOrden estado) {
        if (estado == null) {
            return ordenCompraRepository.findAll();
        }
        return ordenCompraRepository.findByEstado(estado);
    }

    public OrdenCompra buscarOrdenPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser un número positivo");
        }
        OrdenCompra orden = ordenCompraRepository.findById(id).orElse(null);
        if (orden == null) {
            throw new RecursoNoEncontradoException("Orden de compra no encontrada con id: " + id);
        }
        return orden;
    }

    @Transactional
    public OrdenCompra crearOrden(Long productoId, Long proveedorId, Long bodegaDestinoId, Integer cantidad,
            BigDecimal precioUnitario, Long usuarioAutenticadoId) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor o igual a cero");
        }

        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id: " + productoId);
        }

        Proveedor proveedor = proveedorRepository.findById(proveedorId).orElse(null);
        if (proveedor == null) {
            throw new RecursoNoEncontradoException("Proveedor no encontrado con id: " + proveedorId);
        }

        Bodega bodegaDestino = bodegaRepository.findById(bodegaDestinoId).orElse(null);
        if (bodegaDestino == null) {
            throw new RecursoNoEncontradoException("Bodega no encontrada con id: " + bodegaDestinoId);
        }

        Usuario usuarioAutenticado = usuarioRepository.findById(usuarioAutenticadoId).orElse(null);
        if (usuarioAutenticado == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioAutenticadoId);
        }

        OrdenCompra orden = new OrdenCompra();
        orden.setProductoId(producto);
        orden.setProveedorId(proveedor);
        orden.setBodegaDestinoId(bodegaDestino);
        orden.setCantidad(cantidad);
        orden.setPrecioUnitario(precioUnitario);
        orden.setTotal(precioUnitario.multiply(BigDecimal.valueOf(cantidad)));
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setEstado(EstadoOrden.BORRADOR);
        orden.setCreadoPorId(usuarioAutenticado);

        return ordenCompraRepository.save(orden);
    }

    @Transactional
    public OrdenCompra cambiarEstado(Long ordenId, EstadoOrden nuevoEstado, Long usuarioAutenticadoId) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Debe especificar el nuevo estado de la orden");
        }

        OrdenCompra orden = buscarOrdenPorId(ordenId);
        validarTransicion(orden.getEstado(), nuevoEstado);

        if (nuevoEstado == EstadoOrden.RECIBIDA) {
            List<Long> productoIds = new ArrayList<>();
            productoIds.add(orden.getProductoId().getIdProducto());

            List<Integer> cantidades = new ArrayList<>();
            cantidades.add(orden.getCantidad());

            movimientoService.registrarMovimiento(TipoMovimiento.ENTRADA, usuarioAutenticadoId, null,
                    orden.getBodegaDestinoId().getIdBodega(), productoIds, cantidades);
        }

        // R20: al cambiar el estado, el PDF guardado se elimina
        orden.setPdfGenerado(null);
        orden.setFechaGeneracionPdf(null);
        orden.setEstado(nuevoEstado);

        return ordenCompraRepository.save(orden);
    }

    /**
     * R29/R30: genera el PDF de la orden (datos completos, con marca de
     * agua diagonal BORRADOR si la orden esta en ese estado) y lo
     * guarda en la propia orden para reutilizarlo en obtenerPdf.
     */
    @Transactional
    public byte[] generarPdf(Long ordenId) {
        OrdenCompra orden = buscarOrdenPorId(ordenId);

        byte[] pdfBytes = pdfService.generarPdfOrden(orden);

        orden.setPdfGenerado(pdfBytes);
        orden.setFechaGeneracionPdf(LocalDateTime.now());
        ordenCompraRepository.save(orden);

        return pdfBytes;
    }

    /**
     * Retorna el PDF ya generado y guardado para la orden. Si nunca se
     * genero, o si se invalido por un cambio de estado (R20), lanza
     * RecursoNoEncontradoException.
     */
    public byte[] obtenerPdf(Long ordenId) {
        OrdenCompra orden = buscarOrdenPorId(ordenId);

        if (orden.getPdfGenerado() == null) {
            throw new RecursoNoEncontradoException("La orden no tiene un PDF generado todavia");
        }

        return orden.getPdfGenerado();
    }

    private void validarTransicion(EstadoOrden actual, EstadoOrden nuevo) {
        boolean transicionValida = false;

        if (actual == EstadoOrden.BORRADOR
                && (nuevo == EstadoOrden.APROBADA || nuevo == EstadoOrden.CANCELADA)) {
            transicionValida = true;
        } else if (actual == EstadoOrden.APROBADA
                && (nuevo == EstadoOrden.RECIBIDA || nuevo == EstadoOrden.CANCELADA)) {
            transicionValida = true;
        }

        if (!transicionValida) {
            throw new IllegalArgumentException(
                    "Transición no permitida de " + actual + " a " + nuevo);
        }
    }
}