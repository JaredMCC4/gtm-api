package io.github.jaredmcc4.gtm.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "etiquetas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_etiqueta_usuario_nombre", columnNames = {"usuario_id", "nombre"})
})
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Size(max = 60)
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex;
}
