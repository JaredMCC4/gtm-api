package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Tarea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para operaciones sobre tareas del usuario autenticado.
 */
@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    /**
     * Busca una tarea especifica validando pertenencia a un usuario.
     *
     * @param id identificador de la tarea
     * @param usuarioId propietario de la tarea
     * @return tarea encontrada o vacio
     */
    public Optional<Tarea> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Lista las tareas de un usuario con paginacion.
     *
     * @param usuarioId propietario
     * @param pageable configuracion de pagina
     * @return pagina de tareas
     */
    public Page<Tarea> findByUsuarioId(Long usuarioId, Pageable pageable);

    /**
     * Lista tareas por estado para un usuario.
     *
     * @param usuarioId propietario
     * @param estado estado de la tarea
     * @param pageable configuracion de pagina
     * @return pagina filtrada
     */
    public Page<Tarea> findByUsuarioIdAndEstado(Long usuarioId, Tarea.EstadoTarea estado, Pageable pageable);

    /**
     * Lista tareas por prioridad para un usuario.
     *
     * @param usuarioId propietario
     * @param prioridad prioridad buscada
     * @param pageable configuracion de pagina
     * @return pagina filtrada
     */
    public Page<Tarea> findByUsuarioIdAndPrioridad(Long usuarioId, Tarea.Prioridad prioridad, Pageable pageable);

    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId " +
            "AND (LOWER(t.titulo) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "OR (t.descripcion IS NOT NULL AND LOWER(CAST(t.descripcion AS string)) LIKE LOWER(CONCAT('%', :texto, '%'))))")
    /**
     * Busca tareas por coincidencia parcial en titulo o descripcion.
     *
     * @param usuarioId propietario
     * @param texto texto a buscar
     * @param pageable configuracion de pagina
     * @return pagina con coincidencias
     */
    public Page<Tarea> searchByTexto(@Param("usuarioId") Long usuarioId,
                              @Param("texto") String texto,
                              Pageable pageable);

    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId " +
            "AND (:estado IS NULL OR t.estado = :estado) " +
            "AND (:prioridad IS NULL OR t.prioridad = :prioridad) " +
            "AND (:titulo IS NULL OR LOWER(t.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))")
    /**
     * Aplica filtros opcionales por estado, prioridad y fragmento de titulo.
     *
     * @param usuarioId propietario
     * @param estado estado opcional
     * @param prioridad prioridad opcional
     * @param titulo fragmento de titulo opcional
     * @param pageable configuracion de paginacion
     * @return pagina de tareas filtradas
     */
    public Page<Tarea> findByFilters(@Param("usuarioId") Long usuarioId,
                              @Param("estado") Tarea.EstadoTarea estado,
                              @Param("prioridad") Tarea.Prioridad prioridad,
                              @Param("titulo") String titulo,
                              Pageable pageable);

    // Rango de fechas (vista calendario)
    /**
     * Obtiene tareas dentro de un rango de fechas de vencimiento.
     *
     * @param usuarioId propietario
     * @param inicio fecha inicio inclusiva
     * @param fin fecha fin inclusiva
     * @return lista de tareas dentro del rango
     */
    public List<Tarea> findByUsuarioIdAndFechaVencimientoBetween(Long usuarioId,
                                                          LocalDateTime inicio,
                                                          LocalDateTime fin);

    @Query("SELECT DISTINCT t FROM Tarea t JOIN FETCH t.etiquetas e " +
            "WHERE t.usuario.id = :usuarioId AND e.id = :etiquetaId")
    /**
     * Recupera tareas asociadas a una etiqueta concreta.
     *
     * @param usuarioId propietario
     * @param etiquetaId identificador de la etiqueta
     * @param pageable configuracion de pagina
     * @return pagina de tareas con la etiqueta
     */
    public Page<Tarea> findByUsuarioIdAndEtiquetaId(@Param("usuarioId") Long usuarioId,
                                             @Param("etiquetaId") Long etiquetaId,
                                             Pageable pageable);

    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId " +
            "AND t.estado <> 'COMPLETADA' " +
            "AND t.fechaVencimiento BETWEEN :ahora AND :limite")
    /**
     * Obtiene tareas pendientes cuya fecha de vencimiento cae en una ventana.
     *
     * @param usuarioId propietario
     * @param ahora inicio del rango (normalmente now)
     * @param limite limite superior del rango
     * @return lista de tareas proximas a vencer
     */
    public List<Tarea> findProximasVencer(@Param("usuarioId") Long usuarioId,
                                   @Param("ahora") LocalDateTime ahora,
                                   @Param("limite") LocalDateTime limite);

    /**
     * Cuenta tareas por estado para un usuario.
     *
     * @param usuarioId propietario
     * @param estado estado a contar
     * @return total de tareas en ese estado
     */
    public long countByUsuarioIdAndEstado(Long usuarioId, Tarea.EstadoTarea estado);
}
