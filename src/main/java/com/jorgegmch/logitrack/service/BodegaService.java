package com.jorgegmch.logitrack.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorgegmch.logitrack.entity.Bodega;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.repository.BodegaRepository;
import com.jorgegmch.logitrack.repository.UsuarioRepository;

@Service
public class BodegaService {
    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;

    public BodegaService(BodegaRepository bodegaRepository, UsuarioRepository usuarioRepository) {
        this.bodegaRepository = bodegaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Bodega> listarBodegas() {
        return bodegaRepository.findAll();
    }

    public Bodega buscarBodegaPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser un número positivo");
        }
        Bodega bodega = bodegaRepository.findById(id).orElse(null);
        if (bodega == null) {
            throw new RecursoNoEncontradoException("Bodega no encontrada con el id: " + id);
        }
        return bodega;
    }

    @Transactional
    public Bodega crearBodega(String nombre, String ubicacion, Integer capacidad, Long encargadoId) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("La bodega debe tener un nombre");
        }
        if (ubicacion == null || ubicacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La bodega debe tener una ubicacion");
        }
        if (capacidad == null || capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad de una bodega debe ser mayor a cero");
        }
        Usuario encargado = null;
        if (encargadoId != null) {
            encargado = usuarioRepository.findById(encargadoId).orElse(null);
            if (encargado == null) {
                throw new RecursoNoEncontradoException("Usuario encargado no encontrado con id: " + encargadoId);
            }
        }

        return bodegaRepository.save(new Bodega(null, nombre.trim().toUpperCase(), ubicacion.trim().toUpperCase(), capacidad, encargado));
    }

    @Transactional
    public Bodega actualizarBodega(Long id, String nombre, String ubicacion, Integer capacidad, Long encargadoId) {
        Bodega bodega = buscarBodegaPorId(id);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("La bodega debe tener un nombre");
        }
        if (ubicacion == null || ubicacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La bodega debe tener una ubicacion");
        }
        if (capacidad == null || capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad de una bodega debe ser mayor a cero");
        }

        Usuario encargado = null;
        if (encargadoId != null) {
            encargado = usuarioRepository.findById(encargadoId).orElse(null);
            if (encargado == null) {
                throw new RecursoNoEncontradoException("Usuario encargado no encontrado con id: " + encargadoId);
            }
        }

        bodega.setNombre(nombre.trim().toUpperCase());
        bodega.setUbicacion(ubicacion.trim().toUpperCase());
        bodega.setCapacidad(capacidad);
        bodega.setEncargadoId(encargado);

        return bodegaRepository.save(bodega);
    }

    @Transactional
    public void eliminarBodega(Long id) {
        buscarBodegaPorId(id);
        bodegaRepository.deleteById(id);
    }
}