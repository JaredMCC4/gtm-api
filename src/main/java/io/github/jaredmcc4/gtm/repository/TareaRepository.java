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

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    public Optional<Tarea> findByIdAndUsuarioId(Long id, Long usuarioId);

    public Page<Tarea> findByUsuarioId(Long usuarioId, Pageable pageable);

    public Page<Tarea> findByUsuarioIdAndEstado(Long usuarioId, Tarea.EstadoTarea estado, Pageable pageable);

    public Page<Tarea> findByUsuarioIdAndPrioridad(Long usuarioId, Tarea.Prioridad prioridad, Pageable pageable);

    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId " +
            "AND (LOWER(t.titulo) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "OR (t.descripcion IS NOT NULL AND LOWER(CAST(t.descripcion AS string)) LIKE LOWER(CONCAT('%', :texto, '%'))))")
    public Page<Tarea> searchByTexto(@Param("usuarioId") Long usuarioId,
                              @Param("texto") String texto,
                              Pageable pageable);

    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId " +
            "AND (:estado IS NULL OR t.estado = :estado) " +
            "AND (:prioridad IS NULL OR t.prioridad = :prioridad) " +
            "AND (:titulo IS NULL OR LOWER(t.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))")
    public Page<Tarea> findByFilters(@Param("usuarioId") Long usuarioId,
                              @Param("estado") Tarea.EstadoTarea estado,
                              @Param("prioridad") Tarea.Prioridad prioridad,
                              @Param("titulo") String titulo,
                              Pageable pageable);

    // Rango de fechas (vista calendario)
    public List<Tarea> findByUsuarioIdAndFechaVencimientoBetween(Long usuarioId,
                                                          LocalDateTime inicio,
                                                          LocalDateTime fin);

    @Query("SELECT DISTINCT t FROM Tarea t JOIN FETCH t.etiquetas e " +
            "WHERE t.usuario.id = :usuarioId AND e.id = :etiquetaId")
    public Page<Tarea> findByUsuarioIdAndEtiquetaId(@Param("usuarioId") Long usuarioId,
                                             @Param("etiquetaId") Long etiquetaId,
                                             Pageable pageable);

    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId " +
            "AND t.estado <> 'COMPLETADA' " +
            "AND t.fechaVencimiento BETWEEN :ahora AND :limite")
    public List<Tarea> findProximasVencer(@Param("usuarioId") Long usuarioId,
                                   @Param("ahora") LocalDateTime ahora,
                                   @Param("limite") LocalDateTime limite);

    public long countByUsuarioIdAndEstado(Long usuarioId, Tarea.EstadoTarea estado);
}
