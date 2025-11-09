package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.repository.AdjuntoRepository;
import io.github.jaredmcc4.gtm.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdjuntoServiceImpl implements AdjuntoService{

    private final AdjuntoRepository adjuntoRepository;
    private final TareaRepository tareaRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    @Transactional
    public Adjunto subirAdjunto(Long tareaId, MultipartFile file, Long usuarioId) {
        Tarea tarea = tareaRepository.findByIdAndUsuarioId(tareaId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada o no pertenece al usuario."));
        validarArchivo(file);

        try{
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            String originalFilename = file.getOriginalFilename();
            String sanitizedFilename = (originalFilename == null ? "file" : originalFilename).replaceAll("[\\s]+", "_");
            String finalFilename = UUID.randomUUID() + "_" + sanitizedFilename;
            Path destino = uploadPath.resolve(finalFilename);
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            Adjunto adjunto = Adjunto.builder()
                    .tarea(tarea)
                    .nombre(sanitizedFilename)
                    .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .sizeBytes(file.getSize())
                    .path(destino.toString())
                    .build();

            return adjuntoRepository.save(adjunto);
        } catch (IOException e) {
            throw new IllegalStateException("Error al subir el archivo.", e);
        }
    }

    @Override
    public List<Adjunto> mostrarAdjuntos(Long tareaId, Long usuarioId) {
        confirmarPropiedad(tareaId, usuarioId);
        return adjuntoRepository.findByTareaId(tareaId);
    }

    @Override
    @Transactional
    public void eliminarAdjunto(Long adjuntoId, Long usuarioId) {
        Adjunto adjunto = adjuntoRepository.findByIdAndTareaUsuarioId(adjuntoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Adjunto no encontrado o no pertenece al usuario."));
        try {
            if (adjunto.getPath() != null) {
                Files.deleteIfExists(Paths.get(adjunto.getPath()));
            }
        } catch (IOException ex){
            log.warn("No se pudo eliminar el archivo: {}", adjunto.getPath());
        }
        adjuntoRepository.delete(adjunto);
    }

    private void validarArchivo(MultipartFile file) {
        if (file.isEmpty()){
            throw new IllegalArgumentException("El archivo está vacío.");
        }
        if (file.getSize() > 10 * 1024 * 1024){ // 10MB
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (10MB).");
        }
    }

    private void confirmarPropiedad(Long tareaId, Long usuarioId) {
        if (tareaRepository.findByIdAndUsuarioId(tareaId, usuarioId).isEmpty()){
            throw new UnauthorizedException("No cuenta con permisos para acceder a los adjuntos de esta tarea.");
        }
    }
}
