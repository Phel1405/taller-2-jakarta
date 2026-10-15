package com.app.dtos;

import java.io.Serializable;

/** Solicitud tal como la ven los clientes de Logica y de Datos. */
public class SolicitudDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String descripcion;
    private String recurso;
    private int cantidadRequerida;
    private Prioridad prioridad;
    private EstadoSolicitud estado;

    public SolicitudDTO() {
    }

    public SolicitudDTO(String id, String descripcion, String recurso, int cantidadRequerida,
                        Prioridad prioridad, EstadoSolicitud estado) {
        this.id = id;
        this.descripcion = descripcion;
        this.recurso = recurso;
        this.cantidadRequerida = cantidadRequerida;
        this.prioridad = prioridad;
        this.estado = estado;
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

    @Override
    public String toString() {
        return "SolicitudDTO{id='" + id + "', descripcion='" + descripcion + "', recurso='" + recurso
            + "', cantidadRequerida=" + cantidadRequerida + ", prioridad=" + prioridad
            + ", estado=" + estado + "}";
    }
}
