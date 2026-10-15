package com.app.dtos.excepciones;

/**
 * Base de las excepciones de negocio. Es checked: el contenedor EJB la trata
 * como excepción de aplicación y la entrega tal cual al cliente remoto.
 */
public class NegocioException extends Exception {

    private static final long serialVersionUID = 1L;

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
