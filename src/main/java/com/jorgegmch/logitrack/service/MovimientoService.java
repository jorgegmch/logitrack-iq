package com.jorgegmch.logitrack.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorgegmch.logitrack.entity.Bodega;
import com.jorgegmch.logitrack.entity.DetalleMovimiento;
import com.jorgegmch.logitrack.entity.InventarioBodega;
import com.jorgegmch.logitrack.entity.Movimiento;
import com.jorgegmch.logitrack.entity.Producto;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.entity.enums.TipoMovimiento;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.repository.BodegaRepository;
import com.jorgegmch.logitrack.repository.InventarioBodegaRepository;
import com.jorgegmch.logitrack.repository.MovimientoRepository;
import com.jorgegmch.logitrack.repository.ProductoRepository;
import com.jorgegmch.logitrack.repository.UsuarioRepository;

@Service
public class MovimientoService {
    private final MovimientoRepository movimientoRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioBodegaRepository inventarioBodegaRepository;

    public MovimientoService(MovimientoRepository movimientoRepository, BodegaRepository bodegaRepository,
            ProductoRepository productoRepository, UsuarioRepository usuarioRepository,
            InventarioBodegaRepository inventarioBodegaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.bodegaRepository = bodegaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inventarioBodegaRepository = inventarioBodegaRepository;
    }

    public List<Movimiento> listarMovimientos() {
        return movimientoRepository.findAll();
    }

    public Movimiento buscarMovimientoPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser un número positivo");
        }
        Movimiento movimiento = movimientoRepository.findById(id).orElse(null);
        if (movimiento == null) {
            throw new RecursoNoEncontradoException("Movimiento no encontrado con id: " + id);
        }
        return movimiento;
    }

    public List<Movimiento> listarMovimientosPorRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe especificar fecha de inicio y fecha de fin");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return movimientoRepository.findByFechaBetween(desde, hasta);
    }

    @Transactional
    public Movimiento registrarMovimiento(TipoMovimiento tipo, Long usuarioId, Long bodegaOrigenId,
            Long bodegaDestinoId, List<Long> productoIds, List<Integer> cantidades) {

        if (tipo == null) {
            throw new IllegalArgumentException("El movimiento debe tener un tipo");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("El movimiento debe tener un usuario responsable");
        }
        if (productoIds == null || productoIds.isEmpty()) {
            throw new IllegalArgumentException("El movimiento debe tener al menos un producto");
        }
        if (cantidades == null || cantidades.size() != productoIds.size()) {
            throw new IllegalArgumentException("La lista de cantidades debe tener el mismo tamaño que la lista de productos");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId);
        }

        Bodega bodegaOrigen = null;
        Bodega bodegaDestino = null;

        if (tipo == TipoMovimiento.ENTRADA) {
            if (bodegaOrigenId != null) {
                throw new IllegalArgumentException("Una ENTRADA no debe tener bodega de origen");
            }
            if (bodegaDestinoId == null) {
                throw new IllegalArgumentException("Una ENTRADA debe tener bodega de destino");
            }
            bodegaDestino = buscarBodega(bodegaDestinoId);
        } else if (tipo == TipoMovimiento.SALIDA) {
            if (bodegaDestinoId != null) {
                throw new IllegalArgumentException("Una SALIDA no debe tener bodega de destino");
            }
            if (bodegaOrigenId == null) {
                throw new IllegalArgumentException("Una SALIDA debe tener bodega de origen");
            }
            bodegaOrigen = buscarBodega(bodegaOrigenId);
        } else {
            if (bodegaOrigenId == null || bodegaDestinoId == null) {
                throw new IllegalArgumentException("Una TRANSFERENCIA debe tener bodega de origen y de destino");
            }
            bodegaOrigen = buscarBodega(bodegaOrigenId);
            bodegaDestino = buscarBodega(bodegaDestinoId);
        }

        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setTipo(tipo);
        movimiento.setUsuarioId(usuario);
        movimiento.setBodegaOrigenId(bodegaOrigen);
        movimiento.setBodegaDestinoId(bodegaDestino);

        for (int i = 0; i < productoIds.size(); i++) {
            Long productoId = productoIds.get(i);
            Integer cantidad = cantidades.get(i);

            if (cantidad == null || cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero para el producto con id: " + productoId);
            }

            Producto producto = productoRepository.findById(productoId).orElse(null);
            if (producto == null) {
                throw new RecursoNoEncontradoException("Producto no encontrado con id: " + productoId);
            }

            DetalleMovimiento detalle = new DetalleMovimiento();
            detalle.setMovimiento(movimiento);
            detalle.setProductoId(producto);
            detalle.setCantidad(cantidad);
            movimiento.getDetalles().add(detalle);

            if (tipo == TipoMovimiento.ENTRADA) {
                sumarStock(bodegaDestino, producto, cantidad);
            } else if (tipo == TipoMovimiento.SALIDA) {
                restarStock(bodegaOrigen, producto, cantidad);
            } else {
                restarStock(bodegaOrigen, producto, cantidad);
                sumarStock(bodegaDestino, producto, cantidad);
            }
        }

        return movimientoRepository.save(movimiento);
    }

    private Bodega buscarBodega(Long id) {
        Bodega bodega = bodegaRepository.findById(id).orElse(null);
        if (bodega == null) {
            throw new RecursoNoEncontradoException("Bodega no encontrada con id: " + id);
        }
        return bodega;
    }

    private void sumarStock(Bodega bodega, Producto producto, Integer cantidad) {
        InventarioBodega inventario = inventarioBodegaRepository
                .findByBodegaIdAndProductoId(bodega, producto)
                .orElse(null);

        if (inventario == null) {
            inventario = new InventarioBodega();
            inventario.setBodegaId(bodega);
            inventario.setProductoId(producto);
            inventario.setStock(0);
        }

        inventario.setStock(inventario.getStock() + cantidad);
        inventarioBodegaRepository.save(inventario);
    }

    private void restarStock(Bodega bodega, Producto producto, Integer cantidad) {
        InventarioBodega inventario = inventarioBodegaRepository
                .findByBodegaIdAndProductoId(bodega, producto)
                .orElse(null);

        if (inventario == null || inventario.getStock() < cantidad) {
            throw new IllegalArgumentException(
                "Stock insuficiente del producto " + producto.getNombre() + " en la bodega " + bodega.getNombre());
        }

        inventario.setStock(inventario.getStock() - cantidad);
        inventarioBodegaRepository.save(inventario);
    }
}
