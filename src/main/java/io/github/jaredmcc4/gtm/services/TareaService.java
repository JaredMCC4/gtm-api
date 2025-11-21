package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contrato para operaciones de negocio sobre tareas de un usuario.
 */
public interface TareaService {

    /**
     * Lista tareas del usuario con paginacion y orden.
     *
     * @param usuarioId propietario
     * @param pageable parametros de paginacion
     * @return pagina de tareas
     */
    Page<Tarea> obtenerTareasPorUsuarioId(Long usuarioId, Pageable pageable);

    /**
     * Filtra tareas por estado, titulo parcial y prioridad.
     *
     * @param usuarioId propietario
     * @param estado estado opcional
     * @param titulo fragmento de titulo opcional
     * @param prioridad prioridad opcional
     * @param pageable paginacion
     * @return pagina filtrada
     */
    Page<Tarea> filtrarTareas(Long usuarioId, Tarea.EstadoTarea estado, String titulo, Tarea.Prioridad prioridad, Pageable pageable);

    /**
     * Busca tareas por texto en titulo/descripcion.
     *
     * @param usuarioId propietario
     * @param texto texto a buscar
     * @param pageable paginacion
     * @return pagina con coincidencias
     */
    Page<Tarea> buscarTareasPorTexto(Long usuarioId, String texto, Pageable pageable);

    /**
     * Obtiene tareas asociadas a una etiqueta especifica.
     *
     * @param etiquetaId id de la etiqueta
     * @param usuarioId propietario
     * @param pageable paginacion
     * @return pagina de tareas
     */
    Page<Tarea> obtenerTareasPorEtiquetaId(Long etiquetaId, Long usuarioId, Pageable pageable);

    /**
     * Lista tareas pendientes que vencen dentro de N dias.
     *
     * @param usuarioId propietario
     * @param cantidadDias ventana en dias
     * @return lista de tareas proximas
     */
    List<Tarea> obtenerTareasProximasVencimiento(Long usuarioId, int cantidadDias);

    /**
     * Obtiene una tarea especifica validando propiedad del usuario.
     *
     * @param tareaId id de la tarea
     * @param usuarioId propietario
     * @return tarea encontrada
     */
    Tarea obtenerTareaPorIdYUsuarioId(Long tareaId, Long usuarioId);

    /**
     * Crea una tarea nueva para el usuario.
     *
     * @param tarea datos de la tarea
     * @param usuario propietario
     * @return tarea creada
     */
    Tarea crearTarea(Tarea tarea, Usuario usuario);

    /**
     * Actualiza una tarea ya existente del usuario.
     *
     * @param tareaId id de la tarea
     * @param tareaActualizada datos a modificar
     * @param usuarioId propietario
     * @return tarea actualizada
     */
    Tarea actualizarTarea(Long tareaId, Tarea tareaActualizada, Long usuarioId);

    /**
     * Elimina una tarea y sus dependencias verificando pertenencia al usuario.
     *
     * @param tareaId id de la tarea
     * @param usuarioId propietario
     */
    void eliminarTarea(Long tareaId, Long usuarioId);

    /**
     * Cuenta tareas del usuario agrupando por estado.
     *
     * @param usuarioId propietario
     * @param estado estado a contar
     * @return total de tareas en ese estado
     */
    long contarTareasPorEstado(Long usuarioId, Tarea.EstadoTarea estado);
}
