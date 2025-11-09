package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdjuntoService {

    public Adjunto subirAdjunto(Long tareaId, MultipartFile file, Long usuarioId);

    public List<Adjunto> mostrarAdjuntos(Long tareaId, Long usuarioId);

    public void eliminarAdjunto(Long adjuntoId, Long usuarioId);

}
