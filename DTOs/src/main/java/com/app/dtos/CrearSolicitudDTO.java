package com.app.dtos;

import java.io.Serializable;

/**
 * Datos de entrada para registrar una solicitud. No lleva id ni estado:
 * el id lo genera el tier de datos y el estado inicial lo asigna Logica.
 */
public class CrearSolicitudDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String descripcion;
    private String recurso;
    private int cantidadRequerida;
    private Prioridad prioridad;

    public CrearSolicitudDTO() {
    }

    public CrearSolicitudDTO(String descripcion, String recurso, int cantidadRequerida, Prioridad prioridad) {
        this.descripcion = descripcion;
        this.recurso = recurso;
        this.cantidadRequerida = cantidadRequerida;
        this.prioridad = prioridad;
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

    @Override
    public String toString() {
        return "CrearSolicitudDTO{descripcion='" + descripcion + "', recurso='" + recurso
            + "', cantidadRequerida=" + cantidadRequerida + ", prioridad=" + prioridad + "}";
    }
}
