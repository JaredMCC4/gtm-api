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
    public Subtarea actualizarSubtarea(Long tareaId, Subtarea subtareaActualizada, Long usuarioId){
        Subtarea actual = obtenerSubtareaPropia(tareaId, usuarioId);
        if (subtareaActualizada.getTitulo() != null && !subtareaActualizada.getTitulo().isBlank()) {
            actual.setTitulo(subtareaActualizada.getTitulo());
        }
        if (subtareaActualizada.getCompletada() != null) {
            actual.setCompletada(subtareaActualizada.getCompletada());
        }
        validarSubtarea(actual);
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
        if (subtarea.getTitulo() == null || subtarea.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título de la subtarea no puede estar vacío.");
        }
    }

    private Subtarea obtenerSubtareaPropia(Long subtareaId, Long usuarioId){
        return subtareaRepository.findById(subtareaId)
                .filter(subtarea -> subtarea.getTarea() != null && subtarea.getTarea().getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea no encontrada o no pertenece al usuario."));
    }
}
