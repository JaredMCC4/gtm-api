package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Usuario;

import java.util.List;

/**
 * Contrato para gestionar etiquetas personalizadas de cada usuario.
 */
public interface EtiquetaService {

    /**
     * Lista todas las etiquetas de un usuario.
     *
     * @param usuarioId propietario
     * @return etiquetas asociadas
     */
    List<Etiqueta> obtenerEtiquetasPorUsuarioId(Long usuarioId);

    /**
     * Crea una etiqueta unica (nombre/color) para el usuario.
     *
     * @param etiqueta datos de la etiqueta
     * @param usuario propietario
     * @return etiqueta persistida
     */
    Etiqueta crearEtiqueta(Etiqueta etiqueta, Usuario usuario);

    /**
     * Obtiene una etiqueta del usuario por su ID.
     *
     * @param etiquetaId identificador de la etiqueta
     * @param usuarioId propietario
     * @return etiqueta encontrada
     */
    Etiqueta obtenerEtiquetaPorIdYUsuarioId(Long etiquetaId, Long usuarioId);

    /**
     * Actualiza nombre y color de una etiqueta del usuario.
     *
     * @param etiquetaId identificador de la etiqueta
     * @param etiquetaActualizada datos nuevos
     * @param usuarioId propietario
     * @return etiqueta actualizada
     */
    Etiqueta actualizarEtiqueta(Long etiquetaId, Etiqueta etiquetaActualizada, Long usuarioId);

    /**
     * Elimina una etiqueta del usuario y sus asociaciones.
     *
     * @param etiquetaId identificador de la etiqueta
     * @param usuarioId propietario
     */
    void eliminarEtiqueta(Long etiquetaId, Long usuarioId);

    /**
     * Verifica si existe una etiqueta con el mismo nombre para el usuario.
     *
     * @param nombre nombre propuesto
     * @param usuarioId propietario
     * @return true si ya existe
     */
    boolean existeEtiquetaPorNombreYUsuarioId(String nombre, Long usuarioId);
}
