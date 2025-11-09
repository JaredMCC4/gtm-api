package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Usuario obtenerUsuarioPorId(Long usuarioId){
        log.debug("Obteniendo usuario con ID: {}", usuarioId);
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Long usuarioId, Usuario datosActualizados){
        log.info("Actualizando usuario con ID: {}", usuarioId);
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        if (datosActualizados.getNombreUsuario() != null && !datosActualizados.getNombreUsuario().isBlank()){
            if (datosActualizados.getNombreUsuario().length() > 120){
                throw new IllegalArgumentException("El nombre de usuario no puede ser mayor a 120 caracteres.");
            }
            usuario.setNombreUsuario(datosActualizados.getNombreUsuario());
        }

        if (datosActualizados.getZonaHoraria() != null){
            usuario.setZonaHoraria(datosActualizados.getZonaHoraria());
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva){
        log.info("Cambiando contraseña del usuario con ID: {}", usuarioId);
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        if (!passwordEncoder.matches(passwordActual, usuario.getContrasenaHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }
        if (passwordEncoder.matches(passwordNueva, usuario.getContrasenaHash())) {
            throw new IllegalArgumentException("La contraseña nueva debe ser diferente de la actual.");
        }
        if (passwordNueva == null || passwordNueva.length() < 8) {
            throw new IllegalArgumentException("La contraseña nueva debe tener al menos 8 caracteres.");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }
}
