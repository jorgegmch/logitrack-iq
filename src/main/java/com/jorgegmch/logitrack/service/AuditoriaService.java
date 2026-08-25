package com.jorgegmch.logitrack.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jorgegmch.logitrack.entity.Auditoria;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.entity.enums.TipoOperacion;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.repository.AuditoriaRepository;
import com.jorgegmch.logitrack.repository.UsuarioRepository;

@Service
public class AuditoriaService {
    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Auditoria> listarAuditorias() {
        return auditoriaRepository.findAll();
    }

    public List<Auditoria> buscarPorUsuario(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El id de usuario debe ser un número positivo");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId);
        }
        return auditoriaRepository.findByUsuarioId(usuario);
    }

    public List<Auditoria> buscarPorTipoOperacion(TipoOperacion tipoOperacion) {
        if (tipoOperacion == null) {
            throw new IllegalArgumentException("Debe especificar un tipo de operación");
        }
        return auditoriaRepository.findByTipoOperacion(tipoOperacion);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guardarAuditoria(Auditoria auditoria) {
        auditoriaRepository.save(auditoria);
    }
}
