package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {

    public List<Adjunto> findByTareaId(Long tareaId);
    public void deleteByTareaId(Long tareaId);
}
