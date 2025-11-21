package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Operaciones de lectura y verificacion para roles de seguridad.
 */
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre (ej. ROLE_USER).
     *
     * @param nombreRol nombre exacto del rol
     * @return rol encontrado o vacio
     */
    public Optional<Rol> findByNombreRol(String nombreRol);

    /**
     * Verifica existencia del rol por nombre.
     *
     * @param nombreRol nombre a validar
     * @return true si existe
     */
    public boolean existsByNombreRol(String nombreRol);
}
