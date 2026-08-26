package com.jorgegmch.logitrack.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jorgegmch.logitrack.dto.DesgloseStockBodegaDTO;
import com.jorgegmch.logitrack.dto.ProductoMasMovidoDTO;
import com.jorgegmch.logitrack.entity.DetalleMovimiento;

public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Long> {
    @Query("SELECT new com.jorgegmch.logitrack.dto.ProductoMasMovidoDTO(p.idProducto, p.nombre, SUM(d.cantidad)) "
            + "FROM DetalleMovimiento d JOIN d.productoId p "
            + "GROUP BY p.idProducto, p.nombre "
            + "ORDER BY SUM(d.cantidad) DESC")
    List<ProductoMasMovidoDTO> obtenerProductosMasMovidos();

    // R33: stock total de un producto, calculado desde movimientos.
    // TRANSFERENCIA no afecta el total (resta en origen, suma en destino,
    // ambos dentro del mismo total), por eso se omite del cálculo.
    @Query("SELECT COALESCE(SUM("
            + "CASE "
            + "WHEN m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.ENTRADA THEN dm.cantidad "
            + "WHEN m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.SALIDA THEN -dm.cantidad "
            + "ELSE 0 END"
            + "), 0) "
            + "FROM DetalleMovimiento dm JOIN dm.movimiento m "
            + "WHERE dm.productoId.idProducto = :productoId")
    Long calcularStockTotalProducto(@Param("productoId") Long productoId);

    // R33: desglose de stock por bodega para un producto, calculado desde
    // movimientos (solo bodegas con al menos un movimiento registrado).
    @Query("SELECT new com.jorgegmch.logitrack.dto.DesgloseStockBodegaDTO(b.idBodega, b.nombre, "
            + "SUM("
            + "CASE "
            + "WHEN m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.ENTRADA AND m.bodegaDestinoId.idBodega = b.idBodega THEN dm.cantidad "
            + "WHEN m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.SALIDA AND m.bodegaOrigenId.idBodega = b.idBodega THEN -dm.cantidad "
            + "WHEN m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.TRANSFERENCIA AND m.bodegaDestinoId.idBodega = b.idBodega THEN dm.cantidad "
            + "WHEN m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.TRANSFERENCIA AND m.bodegaOrigenId.idBodega = b.idBodega THEN -dm.cantidad "
            + "ELSE 0 END"
            + ")) "
            + "FROM Bodega b, DetalleMovimiento dm JOIN dm.movimiento m "
            + "WHERE dm.productoId.idProducto = :productoId "
            + "AND (m.bodegaDestinoId.idBodega = b.idBodega OR m.bodegaOrigenId.idBodega = b.idBodega) "
            + "GROUP BY b.idBodega, b.nombre")
    List<DesgloseStockBodegaDTO> calcularDesglosePorBodega(@Param("productoId") Long productoId);

    // R33: stock total de una bodega (todos los productos), calculado
    // desde movimientos. Usado para la ocupación por bodega.
    @Query("SELECT COALESCE(SUM("
            + "CASE "
            + "WHEN m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.ENTRADA AND m.bodegaDestinoId.idBodega = :bodegaId THEN dm.cantidad "
            + "WHEN m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.SALIDA AND m.bodegaOrigenId.idBodega = :bodegaId THEN -dm.cantidad "
            + "WHEN m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.TRANSFERENCIA AND m.bodegaDestinoId.idBodega = :bodegaId THEN dm.cantidad "
            + "WHEN m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.TRANSFERENCIA AND m.bodegaOrigenId.idBodega = :bodegaId THEN -dm.cantidad "
            + "ELSE 0 END"
            + "), 0) "
            + "FROM DetalleMovimiento dm JOIN dm.movimiento m "
            + "WHERE (m.bodegaDestinoId.idBodega = :bodegaId OR m.bodegaOrigenId.idBodega = :bodegaId)")
    Long calcularStockTotalBodega(@Param("bodegaId") Long bodegaId);

    // Consumo: suma de cantidades en movimientos SALIDA de un producto,
    // dentro de un rango de fechas (usado para el consumo diario promedio).
    @Query("SELECT COALESCE(SUM(dm.cantidad), 0) "
            + "FROM DetalleMovimiento dm JOIN dm.movimiento m "
            + "WHERE dm.productoId.idProducto = :productoId "
            + "AND m.tipo = com.jorgegmch.logitrack.entity.enums.TipoMovimiento.SALIDA "
            + "AND m.fecha >= :desde AND m.fecha <= :hasta")
    Long calcularConsumoSalida(@Param("productoId") Long productoId, @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}