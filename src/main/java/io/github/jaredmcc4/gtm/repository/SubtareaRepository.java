package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Subtarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Consultas de persistencia para subtareas vinculadas a una tarea.
 */
public interface SubtareaRepository extends JpaRepository<Subtarea, Long> {

    /**
     * Recupera todas las subtareas pertenecientes a una tarea.
     *
     * @param tareaId id de la tarea padre
     * @return lista de subtareas
     */
    public List<Subtarea> findByTareaId(Long tareaId);

    /**
     * Borra todas las subtareas de una tarea (cascada manual).
     *
     * @param tareaId id de la tarea padre
     */
    public void deleteByTareaId(Long tareaId);
}
