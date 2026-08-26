package com.jorgegmch.logitrack.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jorgegmch.logitrack.dto.DesgloseStockBodegaDTO;
import com.jorgegmch.logitrack.repository.DetalleMovimientoRepository;

@Service
public class StockCalculadoService {
    private final DetalleMovimientoRepository detalleMovimientoRepository;

    public StockCalculadoService(DetalleMovimientoRepository detalleMovimientoRepository) {
        this.detalleMovimientoRepository = detalleMovimientoRepository;
    }

    public Long calcularStockTotalProducto(Long productoId) {
        return detalleMovimientoRepository.calcularStockTotalProducto(productoId);
    }

    public List<DesgloseStockBodegaDTO> calcularDesglosePorBodega(Long productoId) {
        return detalleMovimientoRepository.calcularDesglosePorBodega(productoId);
    }

    public Long calcularStockTotalBodega(Long bodegaId) {
        return detalleMovimientoRepository.calcularStockTotalBodega(bodegaId);
    }

    public Long calcularConsumoSalida(Long productoId, LocalDateTime desde, LocalDateTime hasta) {
        return detalleMovimientoRepository.calcularConsumoSalida(productoId, desde, hasta);
    }
}