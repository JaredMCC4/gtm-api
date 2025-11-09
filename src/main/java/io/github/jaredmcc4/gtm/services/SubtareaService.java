package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Subtarea;

import java.util.List;

public interface SubtareaService {

    public Subtarea crearSubtarea(Long tareaId, Subtarea subtarea, Long usuarioId);
    public Subtarea actualizarSubtarea(Long tareaId, Subtarea subtareaActualizada, Long usuarioId);

    public void eliminarSubtarea(Long subtareaId, Long usuarioId);

    public List<Subtarea> mostrarSubtareas(Long tareaId, Long usuarioId);

}
