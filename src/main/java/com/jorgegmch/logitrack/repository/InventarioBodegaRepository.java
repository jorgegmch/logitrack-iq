package com.jorgegmch.logitrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jorgegmch.logitrack.dto.StockPorBodegaDTO;
import com.jorgegmch.logitrack.entity.Bodega;
import com.jorgegmch.logitrack.entity.InventarioBodega;
import com.jorgegmch.logitrack.entity.Producto;

public interface InventarioBodegaRepository extends JpaRepository<InventarioBodega, Long> {
    Optional<InventarioBodega> findByBodegaIdAndProductoId(Bodega bodegaId, Producto productoId);
    List<InventarioBodega> findByStockLessThan(Integer stock);

    @Query("SELECT new com.jorgegmch.logitrack.dto.StockPorBodegaDTO(b.idBodega, b.nombre, SUM(i.stock)) "
            + "FROM InventarioBodega i JOIN i.bodegaId b "
            + "GROUP BY b.idBodega, b.nombre "
            + "ORDER BY b.nombre")
    List<StockPorBodegaDTO> obtenerStockTotalPorBodega();
}
