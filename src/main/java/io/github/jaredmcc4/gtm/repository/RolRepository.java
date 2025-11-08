package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    public Optional<Rol> findByNombreRol(String nombreRol);
    public boolean existsByNombreRol(String nombreRol);
}
