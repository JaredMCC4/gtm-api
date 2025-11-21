package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar archivos adjuntos asociados a tareas.
 */
public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {

    /**
     * Lista los adjuntos de una tarea.
     *
     * @param tareaId identificador de la tarea
     * @return coleccion de adjuntos
     */
    public List<Adjunto> findByTareaId(Long tareaId);

    /**
     * Busca un adjunto perteneciente a un usuario dado.
     *
     * @param adjuntoId id del adjunto
     * @param usuarioId id del propietario de la tarea
     * @return adjunto encontrado o vacio
     */
    public Optional<Adjunto> findByIdAndTareaUsuarioId(Long adjuntoId, Long usuarioId);

    /**
     * Elimina todos los adjuntos de una tarea.
     *
     * @param tareaId identificador de la tarea
     */
    public void deleteByTareaId(Long tareaId);
}
