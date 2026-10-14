package com.app.logica.interfaces;

import jakarta.ejb.Remote;
import com.app.dtos.CrearSolicitudDTO;
import java.util.List;

@Remote
public interface LogicaServiceRemote {
	void registrarSolicitudes(String descripcion, String recurso, int cantidadRequerida, String prioridad);
	List<CrearSolicitudDTO> consultarSolicitudes();
	CrearSolicitudDTO consultarSolicitud(String id);
	void atenderSolicitud(String id);
	List<CrearSolicitudDTO> consultarSolicitudesPendientes();
}
