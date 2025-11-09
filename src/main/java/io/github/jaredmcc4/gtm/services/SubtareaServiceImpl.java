package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Subtarea;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.SubtareaRepository;
import io.github.jaredmcc4.gtm.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubtareaServiceImpl implements SubtareaService{

    private final TareaRepository tareaRepository;
    private final SubtareaRepository subtareaRepository;

    @Override
    @Transactional
    public Subtarea crearSubtarea(Long tareaId, Subtarea subtarea, Long usuarioId){
        Tarea tarea = tareaRepository.findByIdAndUsuarioId(tareaId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada o no pertenece al usuario."));
        validarSubtarea(subtarea);
        subtarea.setTarea(tarea);
        return subtareaRepository.save(subtarea);
    }

    @Override
    @Transactional
    public Subtarea actualizarSubtarea(Long subtareaId, Subtarea subtareaActualizada, Long usuarioId){
        log.info("Actualizando subtarea con ID: {}\n" +
                "Usuario ID: {}", subtareaId, usuarioId);
        Subtarea actual = obtenerSubtareaPropia(subtareaId, usuarioId);

        if (subtareaActualizada.getTitulo() != null && !subtareaActualizada.getTitulo().isBlank()) {
            if (subtareaActualizada.getTitulo().length() > 120){
                throw new IllegalArgumentException("El título de la subtarea no puede ser mayor a 120 caracteres.");
            }
            actual.setTitulo(subtareaActualizada.getTitulo());
        }
        if (subtareaActualizada.getCompletada() != null) {
            actual.setCompletada(subtareaActualizada.getCompletada());
        }

        return subtareaRepository.save(actual);
    }

    @Override
    @Transactional
    public void eliminarSubtarea(Long subtareaId, Long usuarioId){
        Subtarea actual = obtenerSubtareaPropia(subtareaId, usuarioId);
        subtareaRepository.delete(actual);
    }

    @Override
    public List<Subtarea> mostrarSubtareas(Long tareaId, Long usuarioId){
        tareaRepository.findByIdAndUsuarioId(tareaId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada o no pertenece al usuario."));
        return subtareaRepository.findByTareaId(tareaId);
    }

    private void validarSubtarea(Subtarea subtarea){
        if (subtarea.getTitulo() == null || subtarea.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El título de la subtarea no puede estar vacío.");
        }

        if (subtarea.getTitulo().length() > 120) {
            throw new IllegalArgumentException("El título de la subtarea no puede exceder los 120 caracteres.");
        }
    }

    private Subtarea obtenerSubtareaPropia(Long subtareaId, Long usuarioId){
        return subtareaRepository.findById(subtareaId)
                .filter(subtarea -> subtarea.getTarea() != null && subtarea.getTarea().getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea no encontrada o no pertenece al usuario."));
    }
}
