package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Contrato para gestionar archivos adjuntos asociados a tareas de un usuario.
 */
public interface AdjuntoService {

    /**
     * Guarda un archivo en el sistema de adjuntos y lo vincula a la tarea.
     *
     * @param tareaId identificador de la tarea destino
     * @param file archivo recibido (multipart)
     * @param usuarioId propietario autenticado
     * @return entidad de adjunto creada
     */
    Adjunto subirAdjunto(Long tareaId, MultipartFile file, Long usuarioId);

    /**
     * Obtiene un adjunto verificando propiedad del usuario.
     *
     * @param adjuntoId identificador del adjunto
     * @param usuarioId propietario autenticado
     * @return adjunto encontrado
     */
    Adjunto obtenerAdjuntoPorId(Long adjuntoId, Long usuarioId);

    /**
     * Lista los adjuntos de una tarea valida para el usuario.
     *
     * @param tareaId identificador de la tarea
     * @param usuarioId propietario autenticado
     * @return lista de adjuntos (puede ser vacia)
     */
    List<Adjunto> mostrarAdjuntos(Long tareaId, Long usuarioId);

    /**
     * Elimina un adjunto validando que pertenezca al usuario.
     *
     * @param adjuntoId identificador del adjunto
     * @param usuarioId propietario autenticado
     */
    void eliminarAdjunto(Long adjuntoId, Long usuarioId);

    /**
     * Devuelve el recurso binario de un adjunto del usuario.
     *
     * @param adjuntoId identificador del adjunto
     * @param usuarioId propietario autenticado
     * @return recurso listo para descarga
     */
    Resource descargarAdjunto(Long adjuntoId, Long usuarioId);
}
