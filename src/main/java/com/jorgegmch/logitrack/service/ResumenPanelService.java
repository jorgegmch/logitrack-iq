package com.jorgegmch.logitrack.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jorgegmch.logitrack.dto.AccionSugeridaDTO;
import com.jorgegmch.logitrack.dto.AlertaDTO;
import com.jorgegmch.logitrack.entity.ResumenPanel;
import com.jorgegmch.logitrack.entity.Usuario;
import com.jorgegmch.logitrack.exception.RecursoNoEncontradoException;
import com.jorgegmch.logitrack.repository.BodegaRepository;
import com.jorgegmch.logitrack.repository.OrdenCompraRepository;
import com.jorgegmch.logitrack.repository.ProductoRepository;
import com.jorgegmch.logitrack.repository.ResumenPanelRepository;
import com.jorgegmch.logitrack.repository.UsuarioRepository;

@Service
public class ResumenPanelService {
    private static final String ZONA_BOGOTA = "America/Bogota";

    private final ResumenPanelRepository resumenPanelRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final BodegaRepository bodegaRepository;
    private final ObjectMapper objectMapper;

    public ResumenPanelService(ResumenPanelRepository resumenPanelRepository, UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository, OrdenCompraRepository ordenCompraRepository,
            BodegaRepository bodegaRepository) {
        this.resumenPanelRepository = resumenPanelRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.ordenCompraRepository = ordenCompraRepository;
        this.bodegaRepository = bodegaRepository;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public ResumenPanel obtenerUltimoResumen() {
        ResumenPanel resumen = resumenPanelRepository.findTopByOrderByFechaDesc().orElse(null);
        if (resumen == null) {
            throw new RecursoNoEncontradoException("No hay un resumen publicado todavía");
        }
        return resumen;
    }

    @Transactional
    public ResumenPanel publicarResumen(LocalDate fecha, String narrativa, List<AlertaDTO> alertas,
            List<AccionSugeridaDTO> accionesSugeridas, Long usuarioAutenticadoId) {

        validarFecha(fecha);
        validarNarrativa(narrativa);

        if (alertas == null) {
            throw new IllegalArgumentException("El campo alertas es obligatorio, use un arreglo vacío si no aplica");
        }
        if (accionesSugeridas == null) {
            throw new IllegalArgumentException(
                    "El campo accionesSugeridas es obligatorio, use un arreglo vacío si no aplica");
        }

        for (AlertaDTO alerta : alertas) {
            validarAlerta(alerta);
        }
        for (AccionSugeridaDTO accion : accionesSugeridas) {
            validarAccion(accion);
        }

        Usuario autor = usuarioRepository.findById(usuarioAutenticadoId).orElse(null);
        if (autor == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioAutenticadoId);
        }

        String contenidoJson = construirContenidoJson(fecha, narrativa, alertas, accionesSugeridas);

        // R11: una nueva publicación para la misma fecha reemplaza el
        // contenido anterior (delete físico, no soft delete) y queda
        // registrada en auditoría vía AuditoriaListener automáticamente.
        Optional<ResumenPanel> anterior = resumenPanelRepository.findByFecha(fecha);
        if (anterior.isPresent()) {
            resumenPanelRepository.delete(anterior.get());
        }

        ResumenPanel resumen = new ResumenPanel();
        resumen.setFecha(fecha);
        resumen.setContenidoJson(contenidoJson);
        resumen.setAutorId(autor);

        return resumenPanelRepository.save(resumen);
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        LocalDate hoyBogota = LocalDate.now(ZoneId.of(ZONA_BOGOTA));
        if (!fecha.equals(hoyBogota)) {
            throw new IllegalArgumentException(
                    "La fecha debe corresponder a la fecha actual en America/Bogota: " + hoyBogota);
        }
    }

    private void validarNarrativa(String narrativa) {
        if (narrativa == null || narrativa.length() < 20 || narrativa.length() > 500) {
            throw new IllegalArgumentException("La narrativa debe tener entre 20 y 500 caracteres");
        }
    }

    private void validarAlerta(AlertaDTO alerta) {
        if (alerta.getSeveridad() == null
                || !(alerta.getSeveridad().equals("BAJA")
                        || alerta.getSeveridad().equals("MEDIA")
                        || alerta.getSeveridad().equals("ALTA"))) {
            throw new IllegalArgumentException("Severidad inválida, debe ser BAJA, MEDIA o ALTA");
        }
        if (alerta.getTitulo() == null || alerta.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El título de la alerta es obligatorio");
        }
        if (alerta.getDetalle() == null || alerta.getDetalle().trim().isEmpty()) {
            throw new IllegalArgumentException("El detalle de la alerta es obligatorio");
        }

        int idsInformados = contarIdsInformados(alerta.getProductoId(), alerta.getOrdenId(), alerta.getBodegaId());
        if (idsInformados < 1) {
            throw new IllegalArgumentException("Una alerta debe enlazar al menos un identificador");
        }

        validarExistenciaProducto(alerta.getProductoId());
        validarExistenciaOrden(alerta.getOrdenId());
        validarExistenciaBodega(alerta.getBodegaId());
    }

    private void validarAccion(AccionSugeridaDTO accion) {
        if (accion.getTipo() == null
                || !(accion.getTipo().equals("REVISAR_ORDEN")
                        || accion.getTipo().equals("REVISAR_PRODUCTO")
                        || accion.getTipo().equals("REVISAR_BODEGA"))) {
            throw new IllegalArgumentException(
                    "Tipo de acción inválido, debe ser REVISAR_ORDEN, REVISAR_PRODUCTO o REVISAR_BODEGA");
        }
        if (accion.getDescripcion() == null || accion.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción de la acción es obligatoria");
        }

        int idsInformados = contarIdsInformados(accion.getOrdenId(), accion.getProductoId(), accion.getBodegaId());
        if (idsInformados != 1) {
            throw new IllegalArgumentException("Una acción sugerida debe enlazar exactamente un identificador");
        }

        validarExistenciaOrden(accion.getOrdenId());
        validarExistenciaProducto(accion.getProductoId());
        validarExistenciaBodega(accion.getBodegaId());
    }

    private int contarIdsInformados(Long id1, Long id2, Long id3) {
        int contador = 0;
        if (id1 != null) {
            contador = contador + 1;
        }
        if (id2 != null) {
            contador = contador + 1;
        }
        if (id3 != null) {
            contador = contador + 1;
        }
        return contador;
    }

    private void validarExistenciaProducto(Long productoId) {
        if (productoId != null && !productoRepository.existsById(productoId)) {
            throw new IllegalArgumentException("No existe un producto con id: " + productoId);
        }
    }

    private void validarExistenciaOrden(Long ordenId) {
        if (ordenId != null && !ordenCompraRepository.existsById(ordenId)) {
            throw new IllegalArgumentException("No existe una orden con id: " + ordenId);
        }
    }

    private void validarExistenciaBodega(Long bodegaId) {
        if (bodegaId != null && !bodegaRepository.existsById(bodegaId)) {
            throw new IllegalArgumentException("No existe una bodega con id: " + bodegaId);
        }
    }

    private String construirContenidoJson(LocalDate fecha, String narrativa, List<AlertaDTO> alertas,
            List<AccionSugeridaDTO> accionesSugeridas) {
        Map<String, Object> contenido = new LinkedHashMap<>();
        contenido.put("fecha", fecha);
        contenido.put("narrativa", narrativa);
        contenido.put("alertas", alertas);
        contenido.put("accionesSugeridas", accionesSugeridas);

        try {
            return objectMapper.writeValueAsString(contenido);
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo construir el contenido del resumen: " + e.getMessage());
        }
    }
}