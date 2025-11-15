package io.github.jaredmcc4.gtm.builders;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class TareaTestBuilder {

    private Long id = 1L;
    private Usuario usuario;
    private String titulo = "Tarea de prueba";
    private String descripcion = "Esto es una tarea de prueba con descripción.";
    private Tarea.Prioridad prioridad = Tarea.Prioridad.MEDIA;
    private Tarea.EstadoTarea estadoTarea = Tarea.EstadoTarea.PENDIENTE;
    private LocalDateTime fechaVencimiento = LocalDateTime.now().plusDays(7);
    private Set<Etiqueta> etiquetas = new HashSet<>();

    public static TareaTestBuilder unaTarea() {
        return new TareaTestBuilder();
    }

    public TareaTestBuilder conId(Long id) {
        this.id = id;
        return this;
    }

    public TareaTestBuilder conUsuario(Usuario usuario) {
        this.usuario = usuario;
        return this;
    }

    public TareaTestBuilder conTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public TareaTestBuilder conPrioridad(Tarea.Prioridad prioridad) {
        this.prioridad = prioridad;
        return this;
    }

    public TareaTestBuilder conEstado(Tarea.EstadoTarea estado) {
        this.estadoTarea = estado;
        return this;
    }

    public TareaTestBuilder vencidaHace(int dias) {
        this.fechaVencimiento = LocalDateTime.now().minusDays(dias);
        return this;
    }

    public TareaTestBuilder venceEn(int dias) {
        this.fechaVencimiento = LocalDateTime.now().plusDays(dias);
        return this;
    }

    public TareaTestBuilder conEtiqueta(Etiqueta etiqueta) {
        this.etiquetas.add(etiqueta);
        return this;
    }

    public Tarea build() {
        return Tarea.builder()
                .id(id)
                .usuario(usuario)
                .titulo(titulo)
                .descripcion(descripcion)
                .prioridad(prioridad)
                .estado(estadoTarea)
                .fechaVencimiento(fechaVencimiento)
                .etiquetas(new HashSet<>(etiquetas))
                .build();
    }
}
