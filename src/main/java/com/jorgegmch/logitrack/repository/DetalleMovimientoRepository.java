package com.jorgegmch.logitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jorgegmch.logitrack.dto.ProductoMasMovidoDTO;
import com.jorgegmch.logitrack.entity.DetalleMovimiento;

public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Long> {
    @Query("SELECT new com.jorgegmch.logitrack.dto.ProductoMasMovidoDTO(p.idProducto, p.nombre, SUM(d.cantidad)) "
            + "FROM DetalleMovimiento d JOIN d.productoId p "
            + "GROUP BY p.idProducto, p.nombre "
            + "ORDER BY SUM(d.cantidad) DESC")
    List<ProductoMasMovidoDTO> obtenerProductosMasMovidos();
}
