package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Repositorio JPA para gestion de usuarios y consultas auxiliares por email.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por email sin inicializar sus roles.
     *
     * @param email email unico del usuario
     * @return usuario encontrado o vacio
     */
    public Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email indicado.
     *
     * @param email email a validar
     * @return true si ya existe
     */
    public boolean existsByEmail(String email);

    /**
     * Obtiene un usuario junto con sus roles asociados usando fetch join.
     *
     * @param email email unico del usuario
     * @return usuario con roles cargados o vacio
     */
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    public Optional<Usuario> findByEmailWithRoles(String email);
}
