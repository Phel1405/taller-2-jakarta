package com.app.logica.services;

import com.app.datos.interfaces.DatosServiceRemote;
import com.app.dtos.EstadoSolicitud;
import com.app.dtos.SolicitudDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DatosServiceRemote falso en memoria, escrito a mano para probar Logica sin
 * contenedor ni base de datos. Como el tier real, no valida reglas de negocio
 * y devuelve copias (simula el paso por valor de una llamada remota).
 */
class DatosEnMemoria implements DatosServiceRemote {

    private final Map<String, SolicitudDTO> solicitudes = new LinkedHashMap<>();
    int inserciones;

    @Override
    public SolicitudDTO insertar(SolicitudDTO s) {
        inserciones++;
        SolicitudDTO copia = copiar(s);
        copia.setId(UUID.randomUUID().toString());
        solicitudes.put(copia.getId(), copia);
        return copiar(copia);
    }

    @Override
    public SolicitudDTO buscarPorId(String id) {
        SolicitudDTO s = solicitudes.get(id);
        return s == null ? null : copiar(s);
    }

    @Override
    public List<SolicitudDTO> listarTodas() {
        return solicitudes.values().stream().map(DatosEnMemoria::copiar).toList();
    }

    @Override
    public List<SolicitudDTO> listarPorEstado(EstadoSolicitud e) {
        List<SolicitudDTO> resultado = new ArrayList<>();
        for (SolicitudDTO s : solicitudes.values()) {
            if (s.getEstado() == e) {
                resultado.add(copiar(s));
            }
        }
        return resultado;
    }

    @Override
    public SolicitudDTO actualizarEstado(String id, EstadoSolicitud e) {
        SolicitudDTO s = solicitudes.get(id);
        if (s == null) {
            return null;
        }
        s.setEstado(e);
        return copiar(s);
    }

    private static SolicitudDTO copiar(SolicitudDTO s) {
        return new SolicitudDTO(s.getId(), s.getDescripcion(), s.getRecurso(), s.getCantidadRequerida(),
            s.getPrioridad(), s.getEstado());
    }
}
