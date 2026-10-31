package com.app.datos.mapper;

import com.app.datos.entities.Solicitud;
import com.app.dtos.SolicitudDTO;

/** Conversión entidad <-> DTO: la entidad no sale del tier de datos. */
public final class SolicitudMapper {

    private SolicitudMapper() {
    }

    public static SolicitudDTO aDTO(Solicitud entidad) {
        if (entidad == null) {
            return null;
        }
        return new SolicitudDTO(entidad.getId(), entidad.getDescripcion(), entidad.getRecurso(),
            entidad.getCantidadRequerida(), entidad.getPrioridad(), entidad.getEstado());
    }

    public static Solicitud aEntidad(SolicitudDTO dto) {
        Solicitud entidad = new Solicitud();
        entidad.setId(dto.getId());
        entidad.setDescripcion(dto.getDescripcion());
        entidad.setRecurso(dto.getRecurso());
        entidad.setCantidadRequerida(dto.getCantidadRequerida());
        entidad.setPrioridad(dto.getPrioridad());
        entidad.setEstado(dto.getEstado());
        return entidad;
    }
}
