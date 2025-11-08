package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Usuario;

import java.util.List;

public interface EtiquetaService {

    public List<Etiqueta> obtenerEtiquetasPorUsuarioId(Long usuarioId);

    public Etiqueta crearEtiqueta(Etiqueta etiqueta, Usuario usuario);
    public Etiqueta obtenerEtiquetaPorIdYUsuarioId(Long etiquetaId, Long usuarioId);
    public Etiqueta actualizarEtiqueta(Long etiquetaId, Etiqueta etiquetaActualizada, Long usuarioId);

    public void eliminarEtiqueta(Long etiquetaId, Long usuarioId);

    public boolean existeEtiquetaPorNombreYUsuarioId(String nombre, Long usuarioId);
}
