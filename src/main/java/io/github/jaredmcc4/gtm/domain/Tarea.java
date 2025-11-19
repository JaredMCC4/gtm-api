package io.github.jaredmcc4.gtm.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "tareas", indexes = {
        @Index(name = "idx_tareas_user_estado", columnList = "usuario_id, estado"),
        @Index(name = "idx_tareas_user_prioridad", columnList = "usuario_id, prioridad"),
        @Index(name = "idx_tareas_vencimiento", columnList = "fecha_vencimiento")
})
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Size(min = 3 ,max = 120)
    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Lob
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 10)
    @Builder.Default
    private Prioridad prioridad = Prioridad.MEDIA; // Prioridad x default = Media

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    @Builder.Default
    private EstadoTarea estado = EstadoTarea.PENDIENTE; // Estado x default = Pendiente

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @ManyToMany
    @JoinTable(
            name = "tarea_etiquetas",
            joinColumns = @JoinColumn(name = "tarea_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    @Builder.Default
    private Set<Etiqueta> etiquetas = new HashSet<>();

    public void setEtiquetas(Set<Etiqueta> etiquetas) {
        this.etiquetas = etiquetas == null ? new HashSet<>() : new HashSet<>(etiquetas);
    }

    public static class TareaBuilder {
        private Set<Etiqueta> etiquetas = new HashSet<>();

        public TareaBuilder etiquetas(Set<Etiqueta> etiquetas) {
            this.etiquetas = etiquetas == null ? new HashSet<>() : new HashSet<>(etiquetas);
            return this;
        }
    }


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Prioridad {
        BAJA, MEDIA, ALTA
    }

    public enum EstadoTarea {
        PENDIENTE, COMPLETADA, CANCELADA
    }


}
