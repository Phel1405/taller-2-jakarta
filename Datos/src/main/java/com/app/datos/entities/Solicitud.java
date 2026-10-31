package com.app.datos.entities;

import com.app.dtos.EstadoSolicitud;
import com.app.dtos.Prioridad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA. Nunca sale del tier de datos: DatosService la convierte a
 * SolicitudDTO antes de devolverla.
 */
@Entity
@Table(name = "solicitud")
public class Solicitud {

    /** UUID en texto, generado por el tier de datos al insertar. */
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String recurso;

    @Column(name = "cantidad_requerida", nullable = false)
    private int cantidadRequerida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoSolicitud estado;

    public Solicitud() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRecurso() {
        return recurso;
    }

    public void setRecurso(String recurso) {
        this.recurso = recurso;
    }

    public int getCantidadRequerida() {
        return cantidadRequerida;
    }

    public void setCantidadRequerida(int cantidadRequerida) {
        this.cantidadRequerida = cantidadRequerida;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Prioridad prioridad) {
        this.prioridad = prioridad;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }
}
