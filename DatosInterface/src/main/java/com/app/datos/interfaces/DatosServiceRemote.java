package com.app.datos.interfaces;

import com.app.dtos.EstadoSolicitud;
import com.app.dtos.SolicitudDTO;

import jakarta.ejb.Remote;

import java.util.List;

/**
 * Fachada remota del tier de datos. Solo almacena, consulta y actualiza:
 * no valida reglas de negocio (eso es responsabilidad de Logica).
 */
@Remote
public interface DatosServiceRemote {

    /** Guarda la solicitud con un id UUID nuevo y la devuelve con ese id. */
    SolicitudDTO insertar(SolicitudDTO s);

    /** Devuelve la solicitud o null si no existe. */
    SolicitudDTO buscarPorId(String id);

    List<SolicitudDTO> listarTodas();

    List<SolicitudDTO> listarPorEstado(EstadoSolicitud e);

    /** Cambia el estado y devuelve la solicitud actualizada, o null si no existe. */
    SolicitudDTO actualizarEstado(String id, EstadoSolicitud e);
}
