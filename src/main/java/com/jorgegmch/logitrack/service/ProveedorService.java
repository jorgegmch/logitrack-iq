package com.jorgegmch.logitrack.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jorgegmch.logitrack.entity.Proveedor;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.repository.ProveedorRepository;

@Service
public class ProveedorService {
    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAll();
    }

    public Proveedor buscarProveedorPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser un número positivo");
        }
        Proveedor proveedor = proveedorRepository.findById(id).orElse(null);
        if (proveedor == null) {
            throw new RecursoNoEncontradoException("Proveedor no encontrado con id: " + id);
        }
        return proveedor;
    }
}