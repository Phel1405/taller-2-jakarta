package com.app.dtos.excepciones;

/** Los datos de la solicitud no cumplen las reglas de negocio. */
public class SolicitudInvalidaException extends NegocioException {

    private static final long serialVersionUID = 1L;

    public SolicitudInvalidaException(String mensaje) {
        super("Solicitud inválida: " + mensaje);
    }
}
