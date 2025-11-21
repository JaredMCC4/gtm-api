package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para etiquetas personalizadas asociadas a cada usuario.
 */
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    /**
     * Lista todas las etiquetas creadas por un usuario.
     *
     * @param usuarioId id del propietario
     * @return coleccion de etiquetas
     */
    public List<Etiqueta> findByUsuarioId(Long usuarioId);

    /**
     * Busca una etiqueta concreta por nombre y usuario.
     *
     * @param usuarioId id del propietario
     * @param nombre nombre unico dentro del usuario
     * @return etiqueta encontrada o vacio
     */
    public Optional<Etiqueta> findByUsuarioIdAndNombre(Long usuarioId, String nombre);

    /**
     * Indica si un usuario ya tiene una etiqueta con el nombre dado.
     *
     * @param usuarioId id del propietario
     * @param nombre nombre a validar
     * @return true si ya existe
     */
    default boolean existsByUsuarioIdAndNombre(Long usuarioId, String nombre) {
        return findByUsuarioIdAndNombre(usuarioId, nombre)
                .map(etiqueta -> etiqueta.getNombre().equals(nombre))
                .orElse(false);
    }
}
