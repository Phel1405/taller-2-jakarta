package com.app.logica.interfaces;

import com.app.dtos.CrearSolicitudDTO;
import com.app.dtos.SolicitudDTO;
import com.app.dtos.excepciones.SolicitudInvalidaException;
import com.app.dtos.excepciones.SolicitudNoEncontradaException;
import com.app.dtos.excepciones.SolicitudYaAtendidaException;

import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface LogicaServiceRemote {

    /** Registra una solicitud nueva en estado PENDIENTE y la devuelve con su id. */
    SolicitudDTO registrarSolicitudes(CrearSolicitudDTO solicitud) throws SolicitudInvalidaException;

    List<SolicitudDTO> consultarSolicitudes();

    SolicitudDTO consultarSolicitud(String id)
        throws SolicitudInvalidaException, SolicitudNoEncontradaException;

    /** Cambia una solicitud de PENDIENTE a ATENDIDA. */
    SolicitudDTO atenderSolicitud(String id)
        throws SolicitudInvalidaException, SolicitudNoEncontradaException, SolicitudYaAtendidaException;

    List<SolicitudDTO> consultarSolicitudesPendientes();
}
