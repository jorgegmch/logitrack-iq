package com.jorgegmch.logitrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jorgegmch.logitrack.entity.OrdenCompra;
import com.jorgegmch.logitrack.entity.enums.EstadoOrden;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    List<OrdenCompra> findByEstado(EstadoOrden estado);
}