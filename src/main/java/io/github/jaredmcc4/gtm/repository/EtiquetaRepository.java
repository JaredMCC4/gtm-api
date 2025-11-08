package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {
    public List<Etiqueta> findByUsuarioId(Long usuarioId);
    public Optional<Etiqueta> findByUsuarioIdAndNombre(Long usuarioId, String nombre);
    public boolean existsByUsuarioIdAndNombre(Long usuarioId, String nombre);
}
