package com.app.logica.services;

import com.app.datos.interfaces.DatosServiceRemote;
import com.app.dtos.CrearSolicitudDTO;
import com.app.dtos.EstadoSolicitud;
import com.app.dtos.SolicitudDTO;
import com.app.dtos.excepciones.SolicitudInvalidaException;
import com.app.dtos.excepciones.SolicitudNoEncontradaException;
import com.app.dtos.excepciones.SolicitudYaAtendidaException;
import com.app.logica.interfaces.LogicaServiceRemote;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.util.List;

/**
 * Tier de negocio: aplica TODAS las reglas y delega el almacenamiento en la
 * fachada remota del tier de datos. Solo conoce DTOs y DatosServiceRemote.
 *
 * NOT_SUPPORTED: Logica no tiene recursos transaccionales propios; cada
 * llamada a Datos usa su propia transacción local en el otro servidor.
 * JNDI: ejb:LogicaEAR/Logica/LogicaService!com.app.logica.interfaces.LogicaServiceRemote
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class LogicaService implements LogicaServiceRemote {

    private DatosServiceRemote datos;

    public LogicaService() {
    }

    /** Para pruebas: permite usar un DatosServiceRemote falso en memoria. */
    LogicaService(DatosServiceRemote datos) {
        this.datos = datos;
    }

    @Override
    public SolicitudDTO registrarSolicitudes(CrearSolicitudDTO solicitud) throws SolicitudInvalidaException {
        if (solicitud == null) {
            throw new SolicitudInvalidaException("los datos de la solicitud son obligatorios.");
        }
        if (estaVacio(solicitud.getDescripcion())) {
            throw new SolicitudInvalidaException("la descripción del desastre es obligatoria.");
        }
        if (estaVacio(solicitud.getRecurso())) {
            throw new SolicitudInvalidaException("el tipo de recurso es obligatorio.");
        }
        if (solicitud.getCantidadRequerida() <= 0) {
            throw new SolicitudInvalidaException("la cantidad requerida debe ser mayor que cero (se recibió "
                + solicitud.getCantidadRequerida() + ").");
        }
        if (solicitud.getPrioridad() == null) {
            throw new SolicitudInvalidaException("la prioridad es obligatoria (ALTA, MEDIA o BAJA).");
        }

        SolicitudDTO nueva = new SolicitudDTO(null, solicitud.getDescripcion(), solicitud.getRecurso(),
            solicitud.getCantidadRequerida(), solicitud.getPrioridad(), EstadoSolicitud.PENDIENTE);
        return datos().insertar(nueva);
    }

    @Override
    public List<SolicitudDTO> consultarSolicitudes() {
        return datos().listarTodas();
    }

    @Override
    public SolicitudDTO consultarSolicitud(String id)
            throws SolicitudInvalidaException, SolicitudNoEncontradaException {
        validarId(id);
        SolicitudDTO solicitud = datos().buscarPorId(id);
        if (solicitud == null) {
            throw new SolicitudNoEncontradaException(id);
        }
        return solicitud;
    }

    @Override
    public SolicitudDTO atenderSolicitud(String id)
            throws SolicitudInvalidaException, SolicitudNoEncontradaException, SolicitudYaAtendidaException {
        SolicitudDTO solicitud = consultarSolicitud(id);
        if (solicitud.getEstado() == EstadoSolicitud.ATENDIDA) {
            throw new SolicitudYaAtendidaException(id);
        }
        SolicitudDTO atendida = datos().actualizarEstado(id, EstadoSolicitud.ATENDIDA);
        if (atendida == null) {
            throw new SolicitudNoEncontradaException(id);
        }
        return atendida;
    }

    @Override
    public List<SolicitudDTO> consultarSolicitudesPendientes() {
        return datos().listarPorEstado(EstadoSolicitud.PENDIENTE);
    }

    private DatosServiceRemote datos() {
        if (datos == null) {
            datos = DatosServiceLocator.buscar();
        }
        return datos;
    }

    private static void validarId(String id) throws SolicitudInvalidaException {
        if (estaVacio(id)) {
            throw new SolicitudInvalidaException("el identificador es obligatorio.");
        }
    }

    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
