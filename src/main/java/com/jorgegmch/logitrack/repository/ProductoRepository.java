package com.jorgegmch.logitrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jorgegmch.logitrack.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
