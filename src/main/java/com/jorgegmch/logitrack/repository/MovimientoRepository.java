package com.jorgegmch.logitrack.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jorgegmch.logitrack.entity.Movimiento;
import com.jorgegmch.logitrack.entity.enums.TipoMovimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT DISTINCT m FROM Movimiento m "
            + "LEFT JOIN m.detalles d "
            + "WHERE (:bodegaId IS NULL OR m.bodegaOrigenId.idBodega = :bodegaId OR m.bodegaDestinoId.idBodega = :bodegaId) "
            + "AND (:productoId IS NULL OR d.productoId.idProducto = :productoId) "
            + "AND (:tipoMovimiento IS NULL OR m.tipo = :tipoMovimiento) "
            + "AND (CAST(:fechaInicio AS timestamp) IS NULL OR m.fecha >= :fechaInicio) "
            + "AND (CAST(:fechaFin AS timestamp) IS NULL OR m.fecha <= :fechaFin) "
            + "ORDER BY m.fecha DESC")
    List<Movimiento> buscarConFiltros(
            @Param("bodegaId") Long bodegaId,
            @Param("productoId") Long productoId,
            @Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}