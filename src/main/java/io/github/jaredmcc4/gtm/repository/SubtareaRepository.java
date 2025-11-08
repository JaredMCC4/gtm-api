package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Subtarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubtareaRepository extends JpaRepository<Subtarea, Long> {

    public List<Subtarea> findByTareaId(Long tareaId);
    public void deleteByTareaId(Long tareaId);
}
