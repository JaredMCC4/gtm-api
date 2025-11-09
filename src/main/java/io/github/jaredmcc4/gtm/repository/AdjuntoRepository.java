package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {

    public List<Adjunto> findByTareaId(Long tareaId);
    public Optional<Adjunto> findByIdAndTareaUsuarioId(Long adjuntoId, Long usuarioId);
    public void deleteByTareaId(Long tareaId);
}
