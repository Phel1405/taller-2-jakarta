package com.app.dtos.excepciones;

/** La solicitud ya está ATENDIDA y no se puede volver a atender. */
public class SolicitudYaAtendidaException extends NegocioException {

    private static final long serialVersionUID = 1L;

    public SolicitudYaAtendidaException(String id) {
        super("La solicitud '" + id + "' ya fue atendida.");
    }
}
