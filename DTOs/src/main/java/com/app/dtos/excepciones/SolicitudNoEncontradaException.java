package com.app.dtos.excepciones;

/** No existe ninguna solicitud con el identificador indicado. */
public class SolicitudNoEncontradaException extends NegocioException {

    private static final long serialVersionUID = 1L;

    public SolicitudNoEncontradaException(String id) {
        super("No existe ninguna solicitud con id '" + id + "'.");
    }
}
