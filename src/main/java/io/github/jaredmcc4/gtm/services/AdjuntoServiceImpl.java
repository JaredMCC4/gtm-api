package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.repository.AdjuntoRepository;
import io.github.jaredmcc4.gtm.validator.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Implementacion de {@link AdjuntoService} que gestiona almacenamiento local
 * de archivos y conserva la referencia en base de datos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdjuntoServiceImpl implements AdjuntoService {

    private final AdjuntoRepository adjuntoRepository;
    private final TareaService tareaService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Guarda el archivo fisico, genera nombre unico y persiste el adjunto vinculado a la tarea.
     */
    @Override
    @Transactional
    public Adjunto subirAdjunto(Long tareaId, MultipartFile file, Long usuarioId) {
        log.info("Subiendo adjunto para tarea ID: {} y usuario ID: {}", tareaId, usuarioId);

        FileValidator.validate(file);

        Tarea tarea = tareaService.obtenerTareaPorIdYUsuarioId(tareaId, usuarioId);

        String nombreArchivo = generarNombreUnico(file.getOriginalFilename());
        Path directorioUsuario = Paths.get(uploadDir, usuarioId.toString());
        Path rutaDestino = directorioUsuario.resolve(nombreArchivo);

        try {
            Files.createDirectories(directorioUsuario);
            Files.copy(file.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            Adjunto adjunto = Adjunto.builder()
                    .tarea(tarea)
                    .nombre(file.getOriginalFilename())
                    .mimeType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .path(rutaDestino.toString())
                    .build();

            return adjuntoRepository.save(adjunto);

        } catch (IOException e) {
            log.error("Error al guardar el archivo: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }

    /**
     * Lista los adjuntos de una tarea, validando propiedad del usuario.
     */
    @Override
    public List<Adjunto> mostrarAdjuntos(Long tareaId, Long usuarioId) {
        log.debug("Mostrando adjuntos para tarea ID: {} Usuario ID: {}", tareaId, usuarioId);
        tareaService.obtenerTareaPorIdYUsuarioId(tareaId, usuarioId);
        return adjuntoRepository.findByTareaId(tareaId);
    }

    /**
     * Elimina el archivo fisico y la referencia en base de datos.
     */
    @Override
    @Transactional
    public void eliminarAdjunto(Long adjuntoId, Long usuarioId) {
        log.info("Eliminando adjunto ID: {} Usuario ID: {}", adjuntoId, usuarioId);
        Adjunto adjunto = obtenerAdjuntoPorId(adjuntoId, usuarioId);

        try {
            Path ruta = Paths.get(adjunto.getPath());
            Files.deleteIfExists(ruta);
            adjuntoRepository.delete(adjunto);
        } catch (IOException e) {
            log.error("Error al eliminar el archivo fisico: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar el archivo: " + e.getMessage());
        }
    }

    /**
     * Devuelve el recurso binario del adjunto, validando que pertenezca al usuario.
     */
    @Override
    public Resource descargarAdjunto(Long adjuntoId, Long usuarioId) {
        log.debug("Descargando adjunto con ID: {} Usuario ID: {}", adjuntoId, usuarioId);

        Adjunto adjunto = obtenerAdjuntoPorId(adjuntoId, usuarioId);

        try {
            Path ruta = Paths.get(adjunto.getPath());
            Resource resource = new UrlResource(ruta.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("El archivo no existe o no puede ser leido.");
            }
        } catch (MalformedURLException e) {
            log.error("Error al formar la URL del archivo: {}", e.getMessage());
            throw new RuntimeException("Error al descargar el archivo: " + e.getMessage());
        }
    }

    /**
     * Obtiene un adjunto y valida la propiedad del usuario.
     */
    @Override
    public Adjunto obtenerAdjuntoPorId(Long adjuntoId, Long usuarioId) {
        log.debug("Obteniendo adjunto con ID: {} Usuario ID: {}", adjuntoId, usuarioId);

        Adjunto adjunto = adjuntoRepository.findById(adjuntoId)
                .orElseThrow(() -> new ResourceNotFoundException("Adjunto no encontrado."));

        if (!adjunto.getTarea().getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene permiso para acceder al recurso.");
        }

        return adjunto;
    }

    /**
     * Crea un nombre unico conservando la extension del archivo original.
     *
     * @param nombreOriginal nombre recibido
     * @return nuevo nombre con UUID
     */
    private String generarNombreUnico(String nombreOriginal) {
        String extension = "";
        int ultimo = nombreOriginal.lastIndexOf('.');

        if (ultimo > 0) {
            extension = nombreOriginal.substring(ultimo);
        }
        return UUID.randomUUID() + extension;
    }
}

