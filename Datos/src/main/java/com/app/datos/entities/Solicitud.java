package com.app.datos.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Solicitud {
	
	@Id
	String id;
	String descripcion;
	String recurso;
	int cantidadRequerida;
	String prioridad;
	String estado;
}
