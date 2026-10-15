package com.app.cliente;

import com.app.dtos.CrearSolicitudDTO;
import com.app.dtos.Prioridad;
import com.app.dtos.SolicitudDTO;
import com.app.dtos.excepciones.NegocioException;
import com.app.dtos.excepciones.SolicitudInvalidaException;
import com.app.dtos.excepciones.SolicitudNoEncontradaException;
import com.app.dtos.excepciones.SolicitudYaAtendidaException;
import com.app.logica.interfaces.LogicaServiceRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cliente remoto de consola: invoca el EJB de Logica por JNDI y recorre los
 * casos del taller. Configuración por variables de entorno:
 * LOGICA_EJB_URL (por defecto remote+http://localhost:8080),
 * LOGICA_EJB_USER (logica-app) y LOGICA_EJB_PASSWORD (logica_pass).
 */
public class Principal {

    private static final String JNDI_LOGICA =
        "ejb:LogicaEAR/Logica/LogicaService!com.app.logica.interfaces.LogicaServiceRemote";

    private static int casos;
    private static int superados;

    /** Un caso del cliente que puede lanzar una excepción de negocio. */
    @FunctionalInterface
    private interface Caso {
        void ejecutar() throws NegocioException;
    }

    public static void main(String[] args) throws Exception {
        // Oculta los mensajes INFO de la librería cliente de WildFly (versiones, XNIO...).
        Logger.getLogger("").setLevel(Level.WARNING);

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        props.put(Context.PROVIDER_URL, entorno("LOGICA_EJB_URL", "remote+http://localhost:8080"));
        props.put(Context.SECURITY_PRINCIPAL, entorno("LOGICA_EJB_USER", "logica-app"));
        props.put(Context.SECURITY_CREDENTIALS, entorno("LOGICA_EJB_PASSWORD", "logica_pass"));

        System.out.println("Conectando a " + props.get(Context.PROVIDER_URL) + " como " + props.get(Context.SECURITY_PRINCIPAL));
        Context contexto = new InitialContext(props);
        LogicaServiceRemote logica = (LogicaServiceRemote) contexto.lookup(JNDI_LOGICA);
        System.out.println("Proxy obtenido: " + JNDI_LOGICA);

        SolicitudDTO[] registradas = new SolicitudDTO[3];
        int[] pendientesAntes = new int[1];

        caso("1. Registrar 3 solicitudes válidas (ALTA, MEDIA, BAJA)", () -> {
            registradas[0] = logica.registrarSolicitudes(
                new CrearSolicitudDTO("Inundación en el barrio norte", "Agua potable", 500, Prioridad.ALTA));
            registradas[1] = logica.registrarSolicitudes(
                new CrearSolicitudDTO("Incendio forestal en la vereda El Alto", "Bomberos", 30, Prioridad.MEDIA));
            registradas[2] = logica.registrarSolicitudes(
                new CrearSolicitudDTO("Deslizamiento en la vía principal", "Mantas", 120, Prioridad.BAJA));
            for (SolicitudDTO s : registradas) {
                System.out.println("     " + s);
            }
        });

        caso("2. Consultar todas las solicitudes", () -> imprimir(logica.consultarSolicitudes()));

        caso("3. Consultar una solicitud por id", () ->
            System.out.println("     " + logica.consultarSolicitud(registradas[0].getId())));

        caso("4. Consultar solicitudes pendientes", () -> {
            List<SolicitudDTO> pendientes = logica.consultarSolicitudesPendientes();
            pendientesAntes[0] = pendientes.size();
            imprimir(pendientes);
        });

        caso("5. Atender una solicitud", () ->
            System.out.println("     " + logica.atenderSolicitud(registradas[0].getId())));

        caso("6. Consultar pendientes otra vez (debe haber una menos)", () -> {
            List<SolicitudDTO> pendientes = logica.consultarSolicitudesPendientes();
            imprimir(pendientes);
            System.out.println("     Antes: " + pendientesAntes[0] + ", ahora: " + pendientes.size());
            if (pendientes.size() != pendientesAntes[0] - 1) {
                throw new IllegalStateException("se esperaba una pendiente menos");
            }
        });

        caso("7. Registrar con cantidad 0 y con cantidad negativa", () -> {
            esperarError(SolicitudInvalidaException.class, () -> logica.registrarSolicitudes(
                new CrearSolicitudDTO("Sismo", "Carpas", 0, Prioridad.ALTA)));
            esperarError(SolicitudInvalidaException.class, () -> logica.registrarSolicitudes(
                new CrearSolicitudDTO("Sismo", "Carpas", -5, Prioridad.ALTA)));
        });

        caso("8. Registrar con descripción vacía", () -> esperarError(SolicitudInvalidaException.class,
            () -> logica.registrarSolicitudes(new CrearSolicitudDTO("   ", "Carpas", 10, Prioridad.ALTA))));

        caso("9. Registrar con recurso nulo", () -> esperarError(SolicitudInvalidaException.class,
            () -> logica.registrarSolicitudes(new CrearSolicitudDTO("Sismo", null, 10, Prioridad.ALTA))));

        caso("10. Registrar con prioridad nula", () -> esperarError(SolicitudInvalidaException.class,
            () -> logica.registrarSolicitudes(new CrearSolicitudDTO("Sismo", "Carpas", 10, null))));

        caso("11. Atender una solicitud inexistente", () -> esperarError(SolicitudNoEncontradaException.class,
            () -> logica.atenderSolicitud("00000000-0000-0000-0000-000000000000")));

        caso("12. Atender de nuevo la solicitud ya atendida", () -> esperarError(SolicitudYaAtendidaException.class,
            () -> logica.atenderSolicitud(registradas[0].getId())));

        caso("13. Consultar una solicitud inexistente", () -> esperarError(SolicitudNoEncontradaException.class,
            () -> logica.consultarSolicitud("00000000-0000-0000-0000-000000000000")));

        System.out.println();
        System.out.println("==================================================");
        System.out.println(" RESUMEN: " + superados + " de " + casos + " casos superados");
        System.out.println("==================================================");

        contexto.close();
        if (superados != casos) {
            System.exit(1);
        }
    }

    private static void caso(String titulo, Caso caso) {
        casos++;
        System.out.println();
        System.out.println(titulo);
        try {
            caso.ejecutar();
            superados++;
            System.out.println("   [OK]");
        } catch (Exception e) {
            System.out.println("   [FALLO] " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** Ejecuta la llamada y comprueba que lance la excepción de negocio esperada. */
    private static void esperarError(Class<? extends NegocioException> esperada, Caso llamada) {
        try {
            llamada.ejecutar();
        } catch (NegocioException e) {
            if (esperada.isInstance(e)) {
                System.out.println("   [ERROR ESPERADO] " + e.getClass().getSimpleName() + ": " + e.getMessage());
                return;
            }
            throw new IllegalStateException("se esperaba " + esperada.getSimpleName() + " pero llegó "
                + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        throw new IllegalStateException("se esperaba " + esperada.getSimpleName() + " y la operación terminó sin error");
    }

    private static void imprimir(List<SolicitudDTO> solicitudes) {
        System.out.println("     Total: " + solicitudes.size());
        for (SolicitudDTO s : solicitudes) {
            System.out.println("     " + s);
        }
    }

    private static String entorno(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }
}
