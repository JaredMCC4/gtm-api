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

/**
 * Entidad que modela una tarea de un usuario, incluyendo priorizacion,
 * estado, fecha de vencimiento y etiquetas asociadas.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private Prioridad prioridad = Prioridad.MEDIA; // Prioridad x default = Media

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoTarea estado = EstadoTarea.PENDIENTE; // Estado x default = Pendiente

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @ManyToMany
    @JoinTable(
            name = "tarea_etiquetas",
            joinColumns = @JoinColumn(name = "tarea_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<Etiqueta> etiquetas = new HashSet<>();

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

    @Builder
    private Tarea(Long id,
                  Usuario usuario,
                  String titulo,
                  String descripcion,
                  Prioridad prioridad,
                  EstadoTarea estado,
                  LocalDateTime fechaVencimiento,
                  Set<Etiqueta> etiquetas,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt) {
        this.id = id;
        this.usuario = usuario;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad != null ? prioridad : Prioridad.MEDIA;
        this.estado = estado != null ? estado : EstadoTarea.PENDIENTE;
        this.fechaVencimiento = fechaVencimiento;
        setEtiquetas(etiquetas);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Reemplaza las etiquetas asegurando que la coleccion nunca sea null.
     *
     * @param etiquetas conjunto de etiquetas a relacionar
     */
    public void setEtiquetas(Set<Etiqueta> etiquetas) {
        this.etiquetas = etiquetas == null ? new HashSet<>() : new HashSet<>(etiquetas);
    }

    /**
     * Devuelve una copia defensiva de las etiquetas vinculadas.
     *
     * @return conjunto de etiquetas de la tarea
     */
    public Set<Etiqueta> getEtiquetas() {
        return new HashSet<>(etiquetas);
    }

}
