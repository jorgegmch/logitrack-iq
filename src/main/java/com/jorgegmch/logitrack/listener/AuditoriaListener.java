package com.jorgegmch.logitrack.listener;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jorgegmch.logitrack.config.ApplicationContextProvider;
import com.jorgegmch.logitrack.entity.Auditoria;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.entity.enums.TipoOperacion;
import com.jorgegmch.logitrack.repository.UsuarioRepository;
import com.jorgegmch.logitrack.service.AuditoriaService;

import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

public class AuditoriaListener {

    private static final ThreadLocal<Map<String, String>> SNAPSHOTS = ThreadLocal.withInitial(HashMap::new);
    private static final ObjectMapper MAPPER = construirMapper();

    private static ObjectMapper construirMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @PostLoad
    public void alCargar(Object entidad) {
        SNAPSHOTS.get().put(clave(entidad), serializar(entidad));
    }

    @PostPersist
    public void alCrear(Object entidad) {
        registrarAuditoria(entidad, TipoOperacion.INSERT, null, serializar(entidad));
    }

    @PostUpdate
    public void alActualizar(Object entidad) {
        String anterior = SNAPSHOTS.get().get(clave(entidad));
        registrarAuditoria(entidad, TipoOperacion.UPDATE, anterior, serializar(entidad));
    }

    @PostRemove
    public void alEliminar(Object entidad) {
        registrarAuditoria(entidad, TipoOperacion.DELETE, serializar(entidad), null);
    }

    private void registrarAuditoria(Object entidad, TipoOperacion tipo, String valoresAnteriores, String valoresNuevos) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            System.out.println("[AUDITORIA-DEBUG] No hay autenticación activa, se omite el registro.");
            return;
        }
        String username = autenticacion.getName();
        System.out.println("[AUDITORIA-DEBUG] Usuario autenticado detectado: " + username);

        String entidadAfectada = entidad.getClass().getSimpleName();
        LocalDateTime fechaHora = LocalDateTime.now();

        Runnable tareaDespuesDelCommit = () -> {
            System.out.println("[AUDITORIA-DEBUG] Ejecutando tarea after-commit para: " + entidadAfectada);
            UsuarioRepository usuarioRepository = ApplicationContextProvider.obtenerBean(UsuarioRepository.class);
            Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

            if (usuario == null) {
                System.out.println("[AUDITORIA-DEBUG] ERROR: no se encontró usuario con username: " + username);
                return;
            }
            System.out.println("[AUDITORIA-DEBUG] Usuario encontrado: " + usuario.getUsername() + ", guardando auditoria...");

            Auditoria auditoria = new Auditoria();
            auditoria.setTipoOperacion(tipo);
            auditoria.setFechaHora(fechaHora);
            auditoria.setUsuarioId(usuario);
            auditoria.setEntidadAfectada(entidadAfectada);
            auditoria.setValoresAnteriores(valoresAnteriores);
            auditoria.setValoresNuevos(valoresNuevos);

            try {
                AuditoriaService auditoriaService = ApplicationContextProvider.obtenerBean(AuditoriaService.class);
                auditoriaService.guardarAuditoria(auditoria);
                System.out.println("[AUDITORIA-DEBUG] Auditoria guardada exitosamente.");
            } catch (Exception e) {
                System.out.println("[AUDITORIA-DEBUG] EXCEPCION al guardar: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            System.out.println("[AUDITORIA-DEBUG] Sincronización de transacción activa, registrando callback afterCommit.");
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    tareaDespuesDelCommit.run();
                }
            });
        } else {
            System.out.println("[AUDITORIA-DEBUG] Sin sincronización activa, ejecutando inmediatamente.");
            tareaDespuesDelCommit.run();
        }
    }

    private String clave(Object entidad) {
        return entidad.getClass().getSimpleName() + "-" + obtenerId(entidad);
    }

    private Object obtenerId(Object entidad) {
        for (Field campo : entidad.getClass().getDeclaredFields()) {
            if (campo.isAnnotationPresent(Id.class)) {
                campo.setAccessible(true);
                try {
                    return campo.get(entidad);
                } catch (IllegalAccessException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private String serializar(Object entidad) {
        try {
            return MAPPER.writeValueAsString(entidad);
        } catch (Exception e) {
            return "No se pudo serializar la entidad: " + e.getMessage();
        }
    }

    public static void limpiarSnapshots() {
        SNAPSHOTS.remove();
    }
}
