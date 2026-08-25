package com.jorgegmch.logitrack.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jorgegmch.logitrack.entity.InventarioBodega;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.repository.InventarioBodegaRepository;

@Service
public class InventarioBodegaService {
    private final InventarioBodegaRepository inventarioBodegaRepository;

    public InventarioBodegaService(InventarioBodegaRepository inventarioBodegaRepository) {
        this.inventarioBodegaRepository = inventarioBodegaRepository;
    }

    public List<InventarioBodega> listarInventario() {
        return inventarioBodegaRepository.findAll();
    }

    public InventarioBodega buscarInventarioBodegaPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser un número positivo");
        }
        InventarioBodega inventarioBodega = inventarioBodegaRepository.findById(id).orElse(null);
        if (inventarioBodega == null) {
            throw new RecursoNoEncontradoException("Registro d inventario no encontrado con el id: " + id);
        }
        return inventarioBodega;
    }

    public List<InventarioBodega> listarStockBajoInventario(Integer limiteStock) {
        if (limiteStock == null || limiteStock <= 0) {
            throw new IllegalArgumentException("El stock solo acepta números positivos");
        }
        return inventarioBodegaRepository.findByStockLessThan(limiteStock);
    }
}
