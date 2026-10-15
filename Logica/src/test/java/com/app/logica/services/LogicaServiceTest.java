package com.app.logica.services;

import com.app.dtos.CrearSolicitudDTO;
import com.app.dtos.EstadoSolicitud;
import com.app.dtos.Prioridad;
import com.app.dtos.SolicitudDTO;
import com.app.dtos.excepciones.SolicitudInvalidaException;
import com.app.dtos.excepciones.SolicitudNoEncontradaException;
import com.app.dtos.excepciones.SolicitudYaAtendidaException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicaServiceTest {

    private DatosEnMemoria datos;
    private LogicaService logica;

    @BeforeEach
    void preparar() {
        datos = new DatosEnMemoria();
        logica = new LogicaService(datos);
    }

    private static CrearSolicitudDTO valida() {
        return new CrearSolicitudDTO("Inundación en el barrio norte", "Agua potable", 100, Prioridad.ALTA);
    }

    private SolicitudDTO registrarValida() throws SolicitudInvalidaException {
        return logica.registrarSolicitudes(valida());
    }

    @Nested
    @DisplayName("Operaciones exitosas")
    class Exitosas {

        @Test
        @DisplayName("Registrar devuelve la solicitud creada con id y en PENDIENTE")
        void registrar() throws Exception {
            SolicitudDTO creada = registrarValida();

            assertNotNull(creada.getId());
            assertEquals("Inundación en el barrio norte", creada.getDescripcion());
            assertEquals("Agua potable", creada.getRecurso());
            assertEquals(100, creada.getCantidadRequerida());
            assertEquals(Prioridad.ALTA, creada.getPrioridad());
            assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
        }

        @Test
        @DisplayName("Consultar todas devuelve todas las solicitudes registradas")
        void consultarTodas() throws Exception {
            registrarValida();
            logica.registrarSolicitudes(new CrearSolicitudDTO("Incendio forestal", "Bomberos", 20, Prioridad.MEDIA));

            assertEquals(2, logica.consultarSolicitudes().size());
        }

        @Test
        @DisplayName("Consultar por id devuelve la solicitud")
        void consultarPorId() throws Exception {
            SolicitudDTO creada = registrarValida();

            SolicitudDTO encontrada = logica.consultarSolicitud(creada.getId());

            assertEquals(creada.getId(), encontrada.getId());
            assertEquals(creada.getDescripcion(), encontrada.getDescripcion());
        }

        @Test
        @DisplayName("Atender cambia el estado de PENDIENTE a ATENDIDA")
        void atender() throws Exception {
            SolicitudDTO creada = registrarValida();

            SolicitudDTO atendida = logica.atenderSolicitud(creada.getId());

            assertEquals(EstadoSolicitud.ATENDIDA, atendida.getEstado());
            assertEquals(EstadoSolicitud.ATENDIDA, logica.consultarSolicitud(creada.getId()).getEstado());
        }

        @Test
        @DisplayName("Consultar pendientes excluye las atendidas")
        void consultarPendientes() throws Exception {
            SolicitudDTO primera = registrarValida();
            SolicitudDTO segunda = registrarValida();
            logica.atenderSolicitud(primera.getId());

            List<SolicitudDTO> pendientes = logica.consultarSolicitudesPendientes();

            assertEquals(1, pendientes.size());
            assertEquals(segunda.getId(), pendientes.get(0).getId());
        }
    }

    @Nested
    @DisplayName("Reglas de negocio")
    class Reglas {

        @ParameterizedTest(name = "cantidad = {0}")
        @ValueSource(ints = {0, -1, -50})
        @DisplayName("La cantidad debe ser mayor que cero")
        void cantidadMayorQueCero(int cantidad) {
            CrearSolicitudDTO s = valida();
            s.setCantidadRequerida(cantidad);

            assertThrows(SolicitudInvalidaException.class, () -> logica.registrarSolicitudes(s));
            assertEquals(0, datos.inserciones, "una solicitud inválida no debe llegar a Datos");
        }

        @ParameterizedTest(name = "descripción = \"{0}\"")
        @NullSource
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("La descripción es obligatoria")
        void descripcionObligatoria(String descripcion) {
            CrearSolicitudDTO s = valida();
            s.setDescripcion(descripcion);

            assertThrows(SolicitudInvalidaException.class, () -> logica.registrarSolicitudes(s));
            assertEquals(0, datos.inserciones);
        }

        @ParameterizedTest(name = "recurso = \"{0}\"")
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("El tipo de recurso es obligatorio")
        void recursoObligatorio(String recurso) {
            CrearSolicitudDTO s = valida();
            s.setRecurso(recurso);

            assertThrows(SolicitudInvalidaException.class, () -> logica.registrarSolicitudes(s));
            assertEquals(0, datos.inserciones);
        }

        @Test
        @DisplayName("La prioridad es obligatoria")
        void prioridadObligatoria() {
            CrearSolicitudDTO s = valida();
            s.setPrioridad(null);

            assertThrows(SolicitudInvalidaException.class, () -> logica.registrarSolicitudes(s));
            assertEquals(0, datos.inserciones);
        }

        @Test
        @DisplayName("Los datos de la solicitud son obligatorios")
        void solicitudNula() {
            assertThrows(SolicitudInvalidaException.class, () -> logica.registrarSolicitudes(null));
        }

        @Test
        @DisplayName("Toda solicitud nueva inicia en PENDIENTE (lo asigna Logica, no Datos)")
        void nuevaIniciaPendiente() throws Exception {
            registrarValida();

            SolicitudDTO guardada = datos.listarTodas().get(0);
            assertEquals(EstadoSolicitud.PENDIENTE, guardada.getEstado());
        }

        @Test
        @DisplayName("No se puede atender una solicitud inexistente")
        void atenderInexistente() {
            assertThrows(SolicitudNoEncontradaException.class, () -> logica.atenderSolicitud("no-existe"));
        }

        @Test
        @DisplayName("No se puede atender una solicitud ya ATENDIDA")
        void atenderYaAtendida() throws Exception {
            SolicitudDTO creada = registrarValida();
            logica.atenderSolicitud(creada.getId());

            assertThrows(SolicitudYaAtendidaException.class, () -> logica.atenderSolicitud(creada.getId()));
        }

        @Test
        @DisplayName("Consultar una solicitud inexistente lanza SolicitudNoEncontradaException")
        void consultarInexistente() {
            assertThrows(SolicitudNoEncontradaException.class, () -> logica.consultarSolicitud("no-existe"));
        }

        @ParameterizedTest(name = "id = \"{0}\"")
        @NullSource
        @ValueSource(strings = {"", "  "})
        @DisplayName("El id es obligatorio al consultar y al atender")
        void idObligatorio(String id) {
            assertThrows(SolicitudInvalidaException.class, () -> logica.consultarSolicitud(id));
            assertThrows(SolicitudInvalidaException.class, () -> logica.atenderSolicitud(id));
        }

        @Test
        @DisplayName("Una solicitud inválida no se guarda: la lista sigue vacía")
        void invalidaNoSeGuarda() {
            assertThrows(SolicitudInvalidaException.class,
                () -> logica.registrarSolicitudes(new CrearSolicitudDTO(" ", null, 0, null)));
            assertTrue(logica.consultarSolicitudes().isEmpty());
        }
    }
}
