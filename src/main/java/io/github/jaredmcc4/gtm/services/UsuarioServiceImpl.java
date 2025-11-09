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
        if (datosActualizados.getNombreUsuario() != null){
            usuario.setNombreUsuario(datosActualizados.getNombreUsuario());
        }
        if (datosActualizados.getEmail() != null){
            usuario.setEmail(datosActualizados.getEmail());
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva){
        log.info("Cambiando password del usuario con ID: {}", usuarioId);
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        if (!passwordEncoder.matches(passwordActual, usuario.getContrasenaHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }
        usuario.setContrasenaHash(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }
}
