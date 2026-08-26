package com.jorgegmch.logitrack.entity;

import java.time.LocalDate;

import com.jorgegmch.logitrack.listener.AuditoriaListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "resumen_panel")
@Data
@EqualsAndHashCode(of = "idResumenPanel")
@NoArgsConstructor
@AllArgsConstructor
public class ResumenPanel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idResumenPanel;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "contenido_json", nullable = false, columnDefinition = "TEXT")
    private String contenidoJson;

    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autorId;
}