package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementacion de {@link TareaService} que aplica validaciones de negocio para tareas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;

    @Override
    public Page<Tarea> obtenerTareasPorUsuarioId(Long usuarioId, Pageable pageable) {
        log.debug("Obteniendo tareas para el usuario con ID: {}", usuarioId);
        return tareaRepository.findByUsuarioId(usuarioId, pageable);
    }

    @Override
    public Page<Tarea> filtrarTareas(Long usuarioId, Tarea.EstadoTarea estado, String titulo, Tarea.Prioridad prioridad, Pageable pageable) {
        log.debug("""
                Filtrando tareas para el usuario con ID: {}
                Estado: {}
                Titulo: {}
                Prioridad: {}""", usuarioId, estado, titulo, prioridad);
        return tareaRepository.findByFilters(usuarioId, estado, prioridad, titulo, pageable);
    }

    @Override
    public Page<Tarea> buscarTareasPorTexto(Long usuarioId, String texto, Pageable pageable) {
        log.debug("Buscando tareas para el usuario con ID: {} Texto: {}", usuarioId, texto);
        return tareaRepository.searchByTexto(usuarioId, texto, pageable);
    }

    @Override
    public Page<Tarea> obtenerTareasPorEtiquetaId(Long etiquetaId, Long usuarioId, Pageable pageable) {
        log.debug("Obteniendo tareas para el usuario con ID: {} Etiqueta ID: {}", usuarioId, etiquetaId);
        return tareaRepository.findByUsuarioIdAndEtiquetaId(usuarioId, etiquetaId, pageable);
    }

    @Override
    public List<Tarea> obtenerTareasProximasVencimiento(Long usuarioId, int cantidadDias) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusDays(cantidadDias);
        log.debug("Obteniendo tareas proximas a vencer Usuario ID: {} Desde: {} Hasta: {}", usuarioId, ahora, limite);
        return tareaRepository.findProximasVencer(usuarioId, ahora, limite);
    }

    @Override
    public Tarea obtenerTareaPorIdYUsuarioId(Long tareaId, Long usuarioId) {
        log.debug("Obteniendo tarea con ID: {} Usuario ID: {}", tareaId, usuarioId);
        return tareaRepository.findByIdAndUsuarioId(tareaId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No encontrada o no pertenece al usuario"));
    }

    @Override
    @Transactional
    public Tarea crearTarea(Tarea tarea, Usuario usuario) {
        log.info("Creando nueva tarea para el usuario con ID: {}", usuario.getId());
        tarea.setUsuario(usuario);
        validarTarea(tarea);
        return tareaRepository.save(tarea);
    }

    @Override
    @Transactional
    public Tarea actualizarTarea(Long tareaId, Tarea tareaActualizada, Long usuarioId) {
        log.info("Actualizando tarea con ID: {} Usuario ID: {}", tareaId, usuarioId);
        Tarea tareaExistente = obtenerTareaPorIdYUsuarioId(tareaId, usuarioId);
        actualizarCamposTarea(tareaExistente, tareaActualizada);
        validarTarea(tareaExistente);
        return tareaRepository.save(tareaExistente);
    }

    @Override
    @Transactional
    public void eliminarTarea(Long tareaId, Long usuarioId) {
        log.info("Eliminando tarea con ID: {} Usuario ID: {}", tareaId, usuarioId);
        Tarea tareaExistente = obtenerTareaPorIdYUsuarioId(tareaId, usuarioId);
        tareaRepository.delete(tareaExistente);
    }

    @Override
    public long contarTareasPorEstado(Long usuarioId, Tarea.EstadoTarea estado) {
        return tareaRepository.countByUsuarioIdAndEstado(usuarioId, estado);
    }

    /**
     * Valida titulo y longitud minima/maxima de la tarea.
     */
    private void validarTarea(Tarea tarea) {
        if (tarea.getTitulo() == null || tarea.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El título de la tarea no puede estar vacío");
        }
        if (tarea.getTitulo().length() < 3 || tarea.getTitulo().length() > 120) {
            throw new IllegalArgumentException("El titulo debe tener Entre 3 y 120 caracteres");
        }
    }

    /**
     * Aplica actualizaciones parciales a la tarea existente.
     */
    private void actualizarCamposTarea(Tarea tareaExistente, Tarea tareaActualizada) {
        if (tareaActualizada.getTitulo() != null) {
            tareaExistente.setTitulo(tareaActualizada.getTitulo());
        }
        if (tareaActualizada.getDescripcion() != null) {
            tareaExistente.setDescripcion(tareaActualizada.getDescripcion());
        }
        if (tareaActualizada.getPrioridad() != null) {
            tareaExistente.setPrioridad(tareaActualizada.getPrioridad());
        }
        if (tareaActualizada.getEstado() != null) {
            tareaExistente.setEstado(tareaActualizada.getEstado());
        }
        if (tareaActualizada.getFechaVencimiento() != null) {
            tareaExistente.setFechaVencimiento(tareaActualizada.getFechaVencimiento());
        }
    }
}
