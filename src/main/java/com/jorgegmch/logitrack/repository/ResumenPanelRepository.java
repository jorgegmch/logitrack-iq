package com.jorgegmch.logitrack.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jorgegmch.logitrack.entity.ResumenPanel;

public interface ResumenPanelRepository extends JpaRepository<ResumenPanel, Long> {
    Optional<ResumenPanel> findByFecha(LocalDate fecha);

    Optional<ResumenPanel> findTopByOrderByFechaDesc();
}