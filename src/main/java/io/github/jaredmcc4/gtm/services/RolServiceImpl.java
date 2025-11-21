package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.RolRepository;
import io.github.jaredmcc4.gtm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion de {@link RolService} para alta y asignacion de roles a usuarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Crea un rol si no existe otro con el mismo nombre.
     */
    @Override
    @Transactional
    public Rol crearRol(Rol rol) {
        if (rolRepository.existsByNombreRol(rol.getNombreRol())) {
            throw new IllegalArgumentException("El rol ya existe.");
        }
        return rolRepository.save(rol);
    }

    /**
     * Devuelve la lista de roles disponibles.
     */
    @Override
    public List<Rol> mostrarRoles() {
        return rolRepository.findAll();
    }

    /**
     * Asigna un rol existente al usuario indicado.
     */
    @Override
    @Transactional
    public void asignarRol(Long usuarioId, String nombreRol) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
        Rol rol = rolRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado."));
        usuario.getRoles().add(rol);
        usuarioRepository.save(usuario);
    }
}

