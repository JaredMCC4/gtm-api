package io.github.jaredmcc4.gtm.builders;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Usuario;

public class EtiquetaTestBuilder {

    private Long id;
    private String nombre = "Etiqueta Test";
    private String colorHex = "#FF5733";
    private Usuario usuario;

    public static EtiquetaTestBuilder unaEtiqueta() {
        return new EtiquetaTestBuilder();
    }

    public EtiquetaTestBuilder conId(Long id) {
        this.id = id;
        return this;
    }

    public EtiquetaTestBuilder conNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public EtiquetaTestBuilder conColor(String color) {
        this.colorHex = color;
        return this;
    }

    public EtiquetaTestBuilder conUsuario(Usuario usuario) {
        this.usuario = usuario;
        return this;
    }

    public Etiqueta build() {
        return Etiqueta.builder()
                .id(id)
                .nombre(nombre)
                .colorHex(colorHex)
                .usuario(usuario)
                .build();
    }
}
