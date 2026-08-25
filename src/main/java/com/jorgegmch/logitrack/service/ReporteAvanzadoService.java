package com.jorgegmch.logitrack.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jorgegmch.logitrack.entity.Auditoria;
import com.jorgegmch.logitrack.entity.Movimiento;
import com.jorgegmch.logitrack.entity.enums.TipoMovimiento;
import com.jorgegmch.logitrack.repository.AuditoriaRepository;
import com.jorgegmch.logitrack.repository.MovimientoRepository;

@Service
public class ReporteAvanzadoService {
    private final MovimientoRepository movimientoRepository;
    private final AuditoriaRepository auditoriaRepository;

    public ReporteAvanzadoService(MovimientoRepository movimientoRepository, AuditoriaRepository auditoriaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    public List<Movimiento> buscarMovimientos(Long bodegaId, Long productoId, TipoMovimiento tipoMovimiento,
            LocalDateTime fechaInicio, LocalDateTime fechaFin) {

        validarRangoFechas(fechaInicio, fechaFin);

        return movimientoRepository.buscarConFiltros(bodegaId, productoId, tipoMovimiento, fechaInicio, fechaFin);
    }

    public List<Auditoria> buscarAuditorias(Long productoId, LocalDateTime fechaInicio, LocalDateTime fechaFin,
            String campoModificado) {

        validarRangoFechas(fechaInicio, fechaFin);

        return auditoriaRepository.buscarConFiltros(productoId, fechaInicio, fechaFin, campoModificado);
    }

    private void validarRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }
}