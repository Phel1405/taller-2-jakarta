# Evidencias de ejecución

Salidas **reales**, copiadas sin editar, de la ejecución del 2026-10-14 (WildFly 40.0.1.Final,
MySQL 8.4, Java 21). Los ids son UUID generados por el tier de datos, así que cambian en
cada ejecución.

## Cómo reproducirlo

Desde la raíz del repositorio:

```bash
# 1. Entorno limpio: borra la base de datos y reconstruye todo
docker compose down -v
docker compose up --build -d
docker compose ps                         # mysql "healthy"; datos y logica "Up"
docker compose logs datos logica | grep WFLYSRV0010

# 2. Cliente con Docker (sin Java)
docker compose run --rm cliente

# 3. Cliente con Maven en el host (Java 21 + Maven)
mvn install -DskipTests
mvn -pl Cliente exec:java

# 4. Pruebas unitarias (sin contenedor)
mvn test

# 5. Filas en MySQL
docker compose exec mysql mysql --default-character-set=utf8mb4 \
    -u taller_user -ptaller_pass taller_solicitudes -t \
    -e "SELECT id, descripcion, recurso, cantidad_requerida, prioridad, estado FROM solicitud ORDER BY estado, prioridad;
        SELECT estado, COUNT(*) AS total FROM solicitud GROUP BY estado;"

# 6. Detener
docker compose down
```

---

## 1. Entorno levantado

`docker compose ps`:

```
SERVICE   STATUS                    PORTS
datos     Up 8 seconds              0.0.0.0:8090->8080/tcp, [::]:8090->8080/tcp
logica    Up 8 seconds              0.0.0.0:8080->8080/tcp, [::]:8080->8080/tcp
mysql     Up 19 seconds (healthy)   33060/tcp, 0.0.0.0:3307->3306/tcp, [::]:3307->3306/tcp
```

Despliegues y datasource (`docker compose logs datos logica`):

```
02:09:29,478 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 48) WFLYSRV0010: Deployed "LogicaEAR.ear" (runtime-name : "LogicaEAR.ear")
02:09:26,405 INFO  [org.jboss.as.connector.subsystems.datasources] (MSC service thread 1-3) WFLYJCA0001: Bound data source [java:jboss/datasources/MySQLDS]
02:09:30,634 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 48) WFLYSRV0010: Deployed "DatosEAR.ear" (runtime-name : "DatosEAR.ear")
```

---

## 2. Cliente ejecutado con Docker (`docker compose run --rm cliente`)

Primera ejecución sobre la base recién creada:

```
Conectando a remote+http://logica:8080 como logica-app
Proxy obtenido: ejb:LogicaEAR/Logica/LogicaService!com.app.logica.interfaces.LogicaServiceRemote

1. Registrar 3 solicitudes válidas (ALTA, MEDIA, BAJA)
     SolicitudDTO{id='1561fc6b-6710-4cca-b82f-e28db0209d38', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=PENDIENTE}
     SolicitudDTO{id='9c08c160-1d40-4391-95b0-5864e84cff3a', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
     SolicitudDTO{id='387e284c-2ef3-4eb4-b34e-8861cf45dcca', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
   [OK]

2. Consultar todas las solicitudes
     Total: 3
     SolicitudDTO{id='1561fc6b-6710-4cca-b82f-e28db0209d38', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=PENDIENTE}
     SolicitudDTO{id='387e284c-2ef3-4eb4-b34e-8861cf45dcca', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
     SolicitudDTO{id='9c08c160-1d40-4391-95b0-5864e84cff3a', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
   [OK]

3. Consultar una solicitud por id
     SolicitudDTO{id='1561fc6b-6710-4cca-b82f-e28db0209d38', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=PENDIENTE}
   [OK]

4. Consultar solicitudes pendientes
     Total: 3
     SolicitudDTO{id='1561fc6b-6710-4cca-b82f-e28db0209d38', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=PENDIENTE}
     SolicitudDTO{id='387e284c-2ef3-4eb4-b34e-8861cf45dcca', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
     SolicitudDTO{id='9c08c160-1d40-4391-95b0-5864e84cff3a', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
   [OK]

5. Atender una solicitud
     SolicitudDTO{id='1561fc6b-6710-4cca-b82f-e28db0209d38', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=ATENDIDA}
   [OK]

6. Consultar pendientes otra vez (debe haber una menos)
     Total: 2
     SolicitudDTO{id='387e284c-2ef3-4eb4-b34e-8861cf45dcca', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
     SolicitudDTO{id='9c08c160-1d40-4391-95b0-5864e84cff3a', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
     Antes: 3, ahora: 2
   [OK]

7. Registrar con cantidad 0 y con cantidad negativa
   [ERROR ESPERADO] SolicitudInvalidaException: Solicitud inválida: la cantidad requerida debe ser mayor que cero (se recibió 0).
   [ERROR ESPERADO] SolicitudInvalidaException: Solicitud inválida: la cantidad requerida debe ser mayor que cero (se recibió -5).
   [OK]

8. Registrar con descripción vacía
   [ERROR ESPERADO] SolicitudInvalidaException: Solicitud inválida: la descripción del desastre es obligatoria.
   [OK]

9. Registrar con recurso nulo
   [ERROR ESPERADO] SolicitudInvalidaException: Solicitud inválida: el tipo de recurso es obligatorio.
   [OK]

10. Registrar con prioridad nula
   [ERROR ESPERADO] SolicitudInvalidaException: Solicitud inválida: la prioridad es obligatoria (ALTA, MEDIA o BAJA).
   [OK]

11. Atender una solicitud inexistente
   [ERROR ESPERADO] SolicitudNoEncontradaException: No existe ninguna solicitud con id '00000000-0000-0000-0000-000000000000'.
   [OK]

12. Atender de nuevo la solicitud ya atendida
   [ERROR ESPERADO] SolicitudYaAtendidaException: La solicitud '1561fc6b-6710-4cca-b82f-e28db0209d38' ya fue atendida.
   [OK]

13. Consultar una solicitud inexistente
   [ERROR ESPERADO] SolicitudNoEncontradaException: No existe ninguna solicitud con id '00000000-0000-0000-0000-000000000000'.
   [OK]

==================================================
 RESUMEN: 13 de 13 casos superados
==================================================
```

---

## 3. Cliente ejecutado desde el host (`mvn -pl Cliente exec:java`)

Segunda ejecución, justo después de la anterior. Por eso en los casos 2 y 4 aparecen
también las 3 solicitudes de la primera ejecución (6 en total, 5 pendientes antes de
atender y 4 después).

<details>
<summary>Salida completa</summary>

```
Conectando a remote+http://localhost:8080 como logica-app
Proxy obtenido: ejb:LogicaEAR/Logica/LogicaService!com.app.logica.interfaces.LogicaServiceRemote

1. Registrar 3 solicitudes válidas (ALTA, MEDIA, BAJA)
     SolicitudDTO{id='4e51f43c-a06c-412d-9cdc-714e748d9905', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=PENDIENTE}
     SolicitudDTO{id='36e1314c-503f-4f04-bc73-6ed7e72a61de', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
     SolicitudDTO{id='20a86869-d87a-4f31-84aa-2b64986caf01', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
   [OK]

2. Consultar todas las solicitudes
     Total: 6
     SolicitudDTO{id='1561fc6b-6710-4cca-b82f-e28db0209d38', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=ATENDIDA}
     SolicitudDTO{id='20a86869-d87a-4f31-84aa-2b64986caf01', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
     SolicitudDTO{id='36e1314c-503f-4f04-bc73-6ed7e72a61de', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
     SolicitudDTO{id='387e284c-2ef3-4eb4-b34e-8861cf45dcca', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
     SolicitudDTO{id='4e51f43c-a06c-412d-9cdc-714e748d9905', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=PENDIENTE}
     SolicitudDTO{id='9c08c160-1d40-4391-95b0-5864e84cff3a', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
   [OK]

3. Consultar una solicitud por id
     SolicitudDTO{id='4e51f43c-a06c-412d-9cdc-714e748d9905', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=PENDIENTE}
   [OK]

4. Consultar solicitudes pendientes
     Total: 5
     SolicitudDTO{id='20a86869-d87a-4f31-84aa-2b64986caf01', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
     SolicitudDTO{id='36e1314c-503f-4f04-bc73-6ed7e72a61de', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
     SolicitudDTO{id='387e284c-2ef3-4eb4-b34e-8861cf45dcca', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
     SolicitudDTO{id='4e51f43c-a06c-412d-9cdc-714e748d9905', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=PENDIENTE}
     SolicitudDTO{id='9c08c160-1d40-4391-95b0-5864e84cff3a', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
   [OK]

5. Atender una solicitud
     SolicitudDTO{id='4e51f43c-a06c-412d-9cdc-714e748d9905', descripcion='Inundación en el barrio norte', recurso='Agua potable', cantidadRequerida=500, prioridad=ALTA, estado=ATENDIDA}
   [OK]

6. Consultar pendientes otra vez (debe haber una menos)
     Total: 4
     SolicitudDTO{id='20a86869-d87a-4f31-84aa-2b64986caf01', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
     SolicitudDTO{id='36e1314c-503f-4f04-bc73-6ed7e72a61de', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
     SolicitudDTO{id='387e284c-2ef3-4eb4-b34e-8861cf45dcca', descripcion='Deslizamiento en la vía principal', recurso='Mantas', cantidadRequerida=120, prioridad=BAJA, estado=PENDIENTE}
     SolicitudDTO{id='9c08c160-1d40-4391-95b0-5864e84cff3a', descripcion='Incendio forestal en la vereda El Alto', recurso='Bomberos', cantidadRequerida=30, prioridad=MEDIA, estado=PENDIENTE}
     Antes: 5, ahora: 4
   [OK]

7. Registrar con cantidad 0 y con cantidad negativa
   [ERROR ESPERADO] SolicitudInvalidaException: Solicitud inválida: la cantidad requerida debe ser mayor que cero (se recibió 0).
   [ERROR ESPERADO] SolicitudInvalidaException: Solicitud inválida: la cantidad requerida debe ser mayor que cero (se recibió -5).
   [OK]

8. Registrar con descripción vacía
   [ERROR ESPERADO] SolicitudInvalidaException: Solicitud inválida: la descripción del desastre es obligatoria.
   [OK]

9. Registrar con recurso nulo
   [ERROR ESPERADO] SolicitudInvalidaException: Solicitud inválida: el tipo de recurso es obligatorio.
   [OK]

10. Registrar con prioridad nula
   [ERROR ESPERADO] SolicitudInvalidaException: Solicitud inválida: la prioridad es obligatoria (ALTA, MEDIA o BAJA).
   [OK]

11. Atender una solicitud inexistente
   [ERROR ESPERADO] SolicitudNoEncontradaException: No existe ninguna solicitud con id '00000000-0000-0000-0000-000000000000'.
   [OK]

12. Atender de nuevo la solicitud ya atendida
   [ERROR ESPERADO] SolicitudYaAtendidaException: La solicitud '4e51f43c-a06c-412d-9cdc-714e748d9905' ya fue atendida.
   [OK]

13. Consultar una solicitud inexistente
   [ERROR ESPERADO] SolicitudNoEncontradaException: No existe ninguna solicitud con id '00000000-0000-0000-0000-000000000000'.
   [OK]

==================================================
 RESUMEN: 13 de 13 casos superados
==================================================
```

</details>

---

## 4. Pruebas unitarias (`mvn test`)

Salida de Surefire:

```
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.app.logica.services.LogicaServiceTest
[INFO] Running Reglas de negocio
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.156 s -- in Reglas de negocio
[INFO] Running Operaciones exitosas
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.013 s -- in Operaciones exitosas
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.183 s -- in com.app.logica.services.LogicaServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
```

```
[INFO] Reactor Summary for taller-2-jakarta 0.1-SNAPSHOT:
[INFO] 
[INFO] taller-2-jakarta ................................... SUCCESS [  0.001 s]
[INFO] DTOs ............................................... SUCCESS [  0.486 s]
[INFO] LogicaInterface .................................... SUCCESS [  0.013 s]
[INFO] DatosInterface ..................................... SUCCESS [  0.011 s]
[INFO] Logica ............................................. SUCCESS [  0.739 s]
[INFO] Datos .............................................. SUCCESS [  0.021 s]
[INFO] Cliente ............................................ SUCCESS [  0.732 s]
[INFO] LogicaEAR .......................................... SUCCESS [  0.154 s]
[INFO] DatosEAR ........................................... SUCCESS [  0.004 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

Detalle por prueba (de `Logica/target/surefire-reports`; los `[n]` son los valores de
las pruebas parametrizadas):

```
OK    | Reglas                                        | nuevaIniciaPendiente
OK    | Reglas                                        | invalidaNoSeGuarda
OK    | Reglas                                        | consultarInexistente
OK    | Reglas                                        | atenderYaAtendida
OK    | Reglas                                        | prioridadObligatoria
OK    | Reglas                                        | descripcionObligatoria(String)[1]
OK    | Reglas                                        | descripcionObligatoria(String)[2]
OK    | Reglas                                        | descripcionObligatoria(String)[3]
OK    | Reglas                                        | descripcionObligatoria(String)[4]
OK    | Reglas                                        | cantidadMayorQueCero(int)[1]
OK    | Reglas                                        | cantidadMayorQueCero(int)[2]
OK    | Reglas                                        | cantidadMayorQueCero(int)[3]
OK    | Reglas                                        | solicitudNula
OK    | Reglas                                        | recursoObligatorio(String)[1]
OK    | Reglas                                        | recursoObligatorio(String)[2]
OK    | Reglas                                        | recursoObligatorio(String)[3]
OK    | Reglas                                        | idObligatorio(String)[1]
OK    | Reglas                                        | idObligatorio(String)[2]
OK    | Reglas                                        | idObligatorio(String)[3]
OK    | Reglas                                        | atenderInexistente
OK    | Exitosas                                      | atender
OK    | Exitosas                                      | registrar
OK    | Exitosas                                      | consultarPorId
OK    | Exitosas                                      | consultarTodas
OK    | Exitosas                                      | consultarPendientes
```

---

## 5. Filas en MySQL

Después de las dos ejecuciones del cliente (3 solicitudes por ejecución; en cada una se
atendió la de prioridad ALTA):

```
+--------------------------------------+----------------------------------------+--------------+--------------------+-----------+-----------+
| id                                   | descripcion                            | recurso      | cantidad_requerida | prioridad | estado    |
+--------------------------------------+----------------------------------------+--------------+--------------------+-----------+-----------+
| 1561fc6b-6710-4cca-b82f-e28db0209d38 | Inundación en el barrio norte          | Agua potable |                500 | ALTA      | ATENDIDA  |
| 4e51f43c-a06c-412d-9cdc-714e748d9905 | Inundación en el barrio norte          | Agua potable |                500 | ALTA      | ATENDIDA  |
| 20a86869-d87a-4f31-84aa-2b64986caf01 | Deslizamiento en la vía principal      | Mantas       |                120 | BAJA      | PENDIENTE |
| 387e284c-2ef3-4eb4-b34e-8861cf45dcca | Deslizamiento en la vía principal      | Mantas       |                120 | BAJA      | PENDIENTE |
| 36e1314c-503f-4f04-bc73-6ed7e72a61de | Incendio forestal en la vereda El Alto | Bomberos     |                 30 | MEDIA     | PENDIENTE |
| 9c08c160-1d40-4391-95b0-5864e84cff3a | Incendio forestal en la vereda El Alto | Bomberos     |                 30 | MEDIA     | PENDIENTE |
+--------------------------------------+----------------------------------------+--------------+--------------------+-----------+-----------+
+-----------+-------+
| estado    | total |
+-----------+-------+
| ATENDIDA  |     2 |
| PENDIENTE |     4 |
+-----------+-------+
```
