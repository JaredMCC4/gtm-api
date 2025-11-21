package io.github.jaredmcc4.gtm.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entidad simple para subtareas pertenecientes a una tarea padre.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "subtareas", indexes = {
        @Index(name = "idx_sub_tarea", columnList = "tarea_id")
})
public class Subtarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id", nullable = false)
    private Tarea tarea;

    @NotBlank
    @Size(max = 120)
    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "completada", nullable = false)
    @Builder.Default
    private Boolean completada = false;
}
