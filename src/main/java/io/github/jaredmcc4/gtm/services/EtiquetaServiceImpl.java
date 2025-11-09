package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.DuplicateResourceException;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.repository.EtiquetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EtiquetaServiceImpl implements EtiquetaService{

    private final EtiquetaRepository etiquetaRepository;
    private static final Pattern COLOR_HEX_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    @Override
    public List<Etiqueta> obtenerEtiquetasPorUsuarioId(Long usuarioId){
        log.debug("Obteniendo etiquetas para el usuario con ID: {}", usuarioId);
        return etiquetaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional
    public Etiqueta crearEtiqueta(Etiqueta etiqueta, Usuario usuario){
        log.info("Creando nueva etiqueta para el usuario con ID: {}", usuario.getId());

        if(existeEtiquetaPorNombreYUsuarioId(etiqueta.getNombre(), usuario.getId())){
            throw new DuplicateResourceException("Ya existe una etiqueta con el nombre: " + etiqueta.getNombre());
        }

        etiqueta.setUsuario(usuario);
        validarEtiqueta(etiqueta);
        return etiquetaRepository.save(etiqueta);
    }

    @Override
    public Etiqueta obtenerEtiquetaPorIdYUsuarioId(Long etiquetaId, Long usuarioId){
        log.debug("Obteniendo etiqueta con ID: {}\n" +
                "Usuario ID: {}", etiquetaId, usuarioId);
        Etiqueta etiqueta = etiquetaRepository.findById(etiquetaId)
                .orElseThrow(() -> new IllegalArgumentException("Etiqueta no encontrada con ID: " + etiquetaId));

        if (!etiqueta.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No cuenta con permisos para acceder a esta etiqueta.");
        }

        return etiqueta;
    }

    @Override
    @Transactional
    public Etiqueta actualizarEtiqueta(Long etiquetaId, Etiqueta etiquetaActualizada, Long usuarioId){
        log.info("Actualizando etiqueta con ID: {}\n" +
                "Usuario ID: {}", etiquetaId, usuarioId);
        Etiqueta etiquetaActual = obtenerEtiquetaPorIdYUsuarioId(etiquetaId, usuarioId);

        if (!etiquetaActual.getNombre().equals(etiquetaActualizada.getNombre()) &&
            existeEtiquetaPorNombreYUsuarioId(etiquetaActualizada.getNombre(), usuarioId)) {
            throw new DuplicateResourceException("Ya existe una etiqueta con el nombre: " + etiquetaActualizada.getNombre());
        }

        etiquetaActual.setNombre(etiquetaActualizada.getNombre());
        etiquetaActual.setColorHex(etiquetaActualizada.getColorHex());
        validarEtiqueta(etiquetaActual);
        return etiquetaRepository.save(etiquetaActual);
    }

    @Override
    @Transactional
    public void eliminarEtiqueta(Long etiquetaId, Long usuarioId){
        log.info("Eliminando etiqueta con ID: {}\n" +
                "Usuario ID: {}", etiquetaId, usuarioId);
        Etiqueta etiquetaExistente = obtenerEtiquetaPorIdYUsuarioId(etiquetaId, usuarioId);
        etiquetaRepository.delete(etiquetaExistente);
    }

    @Override
    public boolean existeEtiquetaPorNombreYUsuarioId(String nombre, Long usuarioId){
        return etiquetaRepository.existsByUsuarioIdAndNombre(usuarioId, nombre);
    }

    private void validarEtiqueta(Etiqueta etiqueta){
        if(etiqueta.getNombre() == null || etiqueta.getNombre().trim().isEmpty()){
            throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío.");
        }

        if (etiqueta.getNombre().length() >60) {
            throw new IllegalArgumentException("El nombre de la etiqueta no puede exceder los 60 caracteres.");
        }

        if(etiqueta.getColorHex() == null || !COLOR_HEX_PATTERN.matcher(etiqueta.getColorHex()).matches()){
            throw new IllegalArgumentException("El color hexadecimal de la etiqueta no es válido. Debe tener el formato #RRGGBB.");
        }
    }
}
