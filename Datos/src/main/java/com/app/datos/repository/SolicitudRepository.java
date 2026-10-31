package com.app.datos.repository;

import com.app.datos.entities.Solicitud;
import com.app.dtos.EstadoSolicitud;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 * Acceso a MySQL con JPA. Se ejecuta dentro de la transacción del EJB que lo
 * llama (DatosService).
 */
@ApplicationScoped
public class SolicitudRepository {

    @PersistenceContext(unitName = "Taller-Database")
    private EntityManager em;

    public void guardar(Solicitud solicitud) {
        em.persist(solicitud);
    }

    public Solicitud buscarPorId(String id) {
        return em.find(Solicitud.class, id);
    }

    public List<Solicitud> listarTodas() {
        return em.createQuery("select s from Solicitud s", Solicitud.class).getResultList();
    }

    public List<Solicitud> listarPorEstado(EstadoSolicitud estado) {
        return em.createQuery("select s from Solicitud s where s.estado = :estado", Solicitud.class)
            .setParameter("estado", estado)
            .getResultList();
    }
}
