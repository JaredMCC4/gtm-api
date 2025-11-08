package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TareaService {

    public Page<Tarea> obtenerTareasPorUsuarioId(Long usuarioId, Pageable pageable);
    public Page<Tarea> filtrarTareas(Long usuarioId, Tarea.EstadoTarea estado, String titulo,Tarea.Prioridad prioridad, Pageable pageable);
    public Page<Tarea> buscarTareasPorTitulo(Long usuarioId, String titulo, Pageable pageable);
    public Page<Tarea> obtenerTareasPorEtiquetaId(Long etiquetaId, Long usuarioId,Pageable pageable);

    public List<Tarea> obtenerTareasProximasVencimiento(Long usuarioId, int cantidadDias);

    public Tarea obtenerTareaPorIdYUsuarioId(Long tareaId, Long usuarioId);
    public Tarea crearTarea(Tarea tarea, Usuario usuario);
    public Tarea actualizarTarea(Long tareaId, Tarea tareaActualizada, Long usuarioId);

    public void eliminarTarea(Long tareaId, Long usuarioId);

    public long contarTareasPorEstado(Long usuarioId, Tarea.EstadoTarea estado);
}
