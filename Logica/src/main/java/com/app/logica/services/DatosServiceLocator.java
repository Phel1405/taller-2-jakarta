package com.app.logica.services;

import com.app.datos.interfaces.DatosServiceRemote;

import jakarta.ejb.EJBException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Obtiene el proxy remoto del tier de datos, que corre en OTRO WildFly.
 * La dirección y las credenciales vienen de las variables de entorno
 * DATOS_EJB_URL, DATOS_EJB_USER y DATOS_EJB_PASSWORD.
 */
public final class DatosServiceLocator {

    static final String JNDI_DATOS =
        "ejb:DatosEAR/Datos/DatosService!com.app.datos.interfaces.DatosServiceRemote";

    private DatosServiceLocator() {
    }

    public static DatosServiceRemote buscar() {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        props.put(Context.PROVIDER_URL, entorno("DATOS_EJB_URL", "remote+http://localhost:8090"));
        props.put(Context.SECURITY_PRINCIPAL, entorno("DATOS_EJB_USER", "datos-app"));
        props.put(Context.SECURITY_CREDENTIALS, entorno("DATOS_EJB_PASSWORD", "datos_pass"));
        try {
            Context contexto = new InitialContext(props);
            return (DatosServiceRemote) contexto.lookup(JNDI_DATOS);
        } catch (NamingException e) {
            throw new EJBException("No se pudo localizar el tier de datos en " + props.get(Context.PROVIDER_URL), e);
        }
    }

    private static String entorno(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }
}
