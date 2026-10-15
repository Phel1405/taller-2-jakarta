package com.app.datos.services;

import com.app.datos.entities.Solicitud;
import com.app.datos.interfaces.DatosServiceRemote;
import com.app.datos.mapper.SolicitudMapper;
import com.app.datos.repository.SolicitudRepository;
import com.app.dtos.EstadoSolicitud;
import com.app.dtos.SolicitudDTO;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

/**
 * Implementación de la fachada del tier de datos. Cada método corre en su
 * propia transacción (REQUIRED por defecto). No aplica reglas de negocio.
 * JNDI: ejb:DatosEAR/Datos/DatosService!com.app.datos.interfaces.DatosServiceRemote
 */
@Stateless
public class DatosService implements DatosServiceRemote {

    @Inject
    private SolicitudRepository repositorio;

    @Override
    public SolicitudDTO insertar(SolicitudDTO s) {
        Solicitud entidad = SolicitudMapper.aEntidad(s);
        entidad.setId(UUID.randomUUID().toString());
        repositorio.guardar(entidad);
        return SolicitudMapper.aDTO(entidad);
    }

    @Override
    public SolicitudDTO buscarPorId(String id) {
        return SolicitudMapper.aDTO(repositorio.buscarPorId(id));
    }

    @Override
    public List<SolicitudDTO> listarTodas() {
        return repositorio.listarTodas().stream().map(SolicitudMapper::aDTO).toList();
    }

    @Override
    public List<SolicitudDTO> listarPorEstado(EstadoSolicitud e) {
        return repositorio.listarPorEstado(e).stream().map(SolicitudMapper::aDTO).toList();
    }

    @Override
    public SolicitudDTO actualizarEstado(String id, EstadoSolicitud e) {
        Solicitud entidad = repositorio.buscarPorId(id);
        if (entidad == null) {
            return null;
        }
        entidad.setEstado(e);
        return SolicitudMapper.aDTO(entidad);
    }
}
