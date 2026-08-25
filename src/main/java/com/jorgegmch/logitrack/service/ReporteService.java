package com.jorgegmch.logitrack.service;

import org.springframework.stereotype.Service;

import com.jorgegmch.logitrack.dto.ReporteResumenResponse;
import com.jorgegmch.logitrack.repository.DetalleMovimientoRepository;
import com.jorgegmch.logitrack.repository.InventarioBodegaRepository;

@Service
public class ReporteService {
    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;

    public ReporteService(InventarioBodegaRepository inventarioBodegaRepository,
            DetalleMovimientoRepository detalleMovimientoRepository) {
        this.inventarioBodegaRepository = inventarioBodegaRepository;
        this.detalleMovimientoRepository = detalleMovimientoRepository;
    }

    public ReporteResumenResponse obtenerResumenGeneral() {
        return new ReporteResumenResponse(
                inventarioBodegaRepository.obtenerStockTotalPorBodega(),
                detalleMovimientoRepository.obtenerProductosMasMovidos());
    }
}
