package io.github.jaredmcc4.gtm.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa a un usuario final con sus roles y metadatos de acceso.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "usuarios", indexes = {
        @Index(name = "idx_usuario_email", columnList = "email", unique = true)
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Email
    @Size(max = 190)
    @Column(name = "email", nullable = false, unique = true, length = 190)
    private String email;

    @NotBlank
    @Size(max = 100)
    @Column(name = "password_hash", nullable = false, length = 100)
    private String contrasenaHash;

    @Size(max = 120)
    @Column(name = "nombre_visible", length = 120)
    private String nombreUsuario;

    @Size(max = 64)
    @Column(name = "zona_horaria", length = 64)
    @Builder.Default
    private String zonaHoraria = "America/Costa_Rica";

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "usuarios_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();

    /**
     * Reemplaza el set de roles asegurando que nunca sea null.
     *
     * @param roles coleccion de roles a asignar
     */
    public void setRoles(Set<Rol> roles) {
        this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
    }

    public static class UsuarioBuilder {

        public UsuarioBuilder roles(Set<Rol> roles) {
            this.roles$value = roles == null ? new HashSet<>() : new HashSet<>(roles);
            this.roles$set = true;
            return this;
        }
    }

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
