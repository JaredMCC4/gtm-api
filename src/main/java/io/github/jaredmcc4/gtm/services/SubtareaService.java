package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Subtarea;

import java.util.List;

/**
 * Contrato para gestionar subtareas pertenecientes a una tarea.
 */
public interface SubtareaService {

    /**
     * Crea una subtarea dentro de una tarea existente.
     *
     * @param tareaId identificador de la tarea padre
     * @param subtarea datos de la subtarea
     * @param usuarioId propietario autenticado
     * @return subtarea creada
     */
    Subtarea crearSubtarea(Long tareaId, Subtarea subtarea, Long usuarioId);

    /**
     * Actualiza titulo/estado de una subtarea.
     *
     * @param subtareaId identificador de la subtarea
     * @param subtareaActualizada datos nuevos
     * @param usuarioId propietario autenticado
     * @return subtarea actualizada
     */
    Subtarea actualizarSubtarea(Long subtareaId, Subtarea subtareaActualizada, Long usuarioId);

    /**
     * Elimina una subtarea validando que pertenezca al usuario.
     *
     * @param subtareaId identificador de la subtarea
     * @param usuarioId propietario autenticado
     */
    void eliminarSubtarea(Long subtareaId, Long usuarioId);

    /**
     * Lista las subtareas de una tarea del usuario.
     *
     * @param tareaId identificador de la tarea padre
     * @param usuarioId propietario autenticado
     * @return lista de subtareas (puede ser vacia)
     */
    List<Subtarea> mostrarSubtareas(Long tareaId, Long usuarioId);
}
