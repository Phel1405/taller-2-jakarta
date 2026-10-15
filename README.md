# Taller 2 – Jakarta EE: tier de negocio con EJB de sesión

Proyecto Maven multi-módulo con una arquitectura de 3 niveles:

| Módulo | Tipo | Descripción |
|---|---|---|
| `DTOs` | jar | Objetos de transferencia compartidos. |
| `LogicaInterface` | jar | Interfaces remotas del tier de negocio. |
| `DatosInterface` | jar | Interfaces remotas del tier de datos. |
| `Logica` | ejb | EJB del tier de negocio. |
| `Datos` | ejb | EJB y entidades JPA del tier de datos. |
| `Cliente` | jar | Cliente de consola (clase `Principal`). |
| `LogicaEAR` | ear | Empaqueta `Logica`. Se despliega en el WildFly de **logica**. |
| `DatosEAR` | ear | Empaqueta `Datos`. Se despliega en el WildFly de **datos**. |

---

## Requisitos

Solo necesitas **Docker** con **Docker Compose v2** (Docker Desktop, o Docker Engine con
el plugin `compose`). Compruébalo con:

```bash
docker compose version
```

**No** necesitas Java, Maven, WildFly ni MySQL instalados: todo se compila y se
ejecuta dentro de contenedores.

---

## Levantar el entorno

Desde la raíz del repositorio:

```bash
docker compose up --build
```

La primera vez tarda unos minutos, porque descarga las imágenes y las dependencias de Maven.
Las siguientes veces es mucho más rápido: las dependencias quedan en una caché de BuildKit.

Este comando:

1. Compila todo el proyecto con Maven dentro de un contenedor y genera `LogicaEAR.ear` y `DatosEAR.ear`.
2. Arranca **mysql** y espera a que esté listo (healthcheck).
3. Arranca **datos**: un WildFly con el datasource `MySQLDS` y `DatosEAR.ear`.
   Hibernate crea la tabla `Solicitud`.
4. Arranca **logica**: un WildFly con `LogicaEAR.ear`.

El entorno está listo cuando en los logs aparecen estas líneas:

```
datos-1   | ... WFLYSRV0010: Deployed "DatosEAR.ear"
logica-1  | ... WFLYSRV0010: Deployed "LogicaEAR.ear"
```

Para dejarlo en segundo plano y recuperar la terminal, usa `-d`:

```bash
docker compose up --build -d
```

> Si cambias código Java, vuelve a ejecutar `docker compose up --build` para recompilar.

### Puertos

| Servicio | URL / puerto en tu máquina | Descripción |
|---|---|---|
| logica | http://localhost:8080 | WildFly del tier de negocio (HTTP y EJB remotos) |
| datos | http://localhost:8090 | WildFly del tier de datos (HTTP y EJB remotos) |
| mysql | `localhost:3307` | MySQL 8.4. Usa 3307 para no chocar con un MySQL local en el 3306. |

---

## Comandos útiles

```bash
docker compose ps                  # estado de los contenedores
docker compose logs -f             # ver los logs de todos (Ctrl+C para salir)
docker compose logs -f datos       # logs de un solo servicio: logica | datos | mysql
docker compose down                # detener y borrar los contenedores (los datos de MySQL se conservan)
docker compose down -v             # detener Y BORRAR los datos de MySQL (volumen mysql_data)
```

### Entrar a MySQL para inspeccionar

```bash
docker compose exec mysql mysql --default-character-set=utf8mb4 -u taller_user -ptaller_pass taller_solicitudes
```

(`--default-character-set=utf8mb4` hace que las tildes se vean bien; sin él, el
cliente `mysql` del contenedor usa `latin1` y muestra `�`, aunque los datos están
guardados correctamente.)

Dentro de MySQL, por ejemplo:

```sql
SHOW TABLES;
DESCRIBE solicitud;
SELECT id, descripcion, prioridad, estado FROM solicitud;
```

También puedes conectarte con un cliente gráfico (DBeaver, MySQL Workbench, etc.) a
`localhost:3307` con el mismo usuario y contraseña.

> **Aviso normal en los logs de `datos`:** si reinicias sin `down -v`, aparece
> `WARN ... Table 'solicitud' already exists`. Es esperado:
> `persistence.xml` usa `schema-generation ... = create` e intenta crear la tabla en
> cada arranque. Si quieres empezar con la base vacía, usa `docker compose down -v`.

> **Cambio de esquema (una sola vez):** la entidad pasó de la tabla `Solicitud`
> (versión inicial) a `solicitud`, con columnas nuevas (`cantidad_requerida`, y
> `prioridad` y `estado` como enums). Como `persistence.xml` solo hace `create`
> (no actualiza tablas), si ya habías levantado el entorno antes de este cambio
> ejecuta **una vez**:
>
> ```bash
> docker compose down -v && docker compose up --build
> ```

---

## Usuarios y credenciales de desarrollo

> ⚠️ Son credenciales **solo para desarrollo local**. No las uses en ningún entorno real.

| Dónde | Usuario | Contraseña | Variables para cambiarlo | Para qué |
|---|---|---|---|---|
| MySQL (root) | `root` | `root_pass` | `MYSQL_ROOT_PASSWORD` | Administración de MySQL |
| MySQL (aplicación) | `taller_user` | `taller_pass` | `MYSQL_USER`, `MYSQL_PASSWORD` | Datasource `MySQLDS` del WildFly de datos |
| Base de datos | `taller_solicitudes` | – | `MYSQL_DATABASE` | Base usada por la unidad `Taller-Database` |
| WildFly **logica** (ApplicationRealm) | `logica-app` | `logica_pass` | `LOGICA_APP_USER`, `LOGICA_APP_PASSWORD` | Invocar los EJB de Logica, por ejemplo desde el `Cliente` en el host |
| WildFly **datos** (ApplicationRealm) | `datos-app` | `datos_pass` | `DATOS_APP_USER`, `DATOS_APP_PASSWORD` | Que Logica invoque los EJB de Datos |

Los nombres de usuario de WildFly usan guion (`-`) porque `add-user.sh` no admite `_`.

### Cambiar valores (opcional)

Funciona **sin** archivo `.env`: `docker-compose.yml` ya tiene todos los valores por
defecto. Para cambiar alguno:

```bash
cp .env.example .env
# edita .env
docker compose down -v          # solo si cambiaste algo de MySQL (los usuarios se crean al iniciar la BD por primera vez)
docker compose up --build       # --build es necesario si cambiaste usuarios de WildFly
```

`.env` no se sube al repositorio (`.gitignore` ignora `*.env`).

### Variables del contenedor `logica`

`DatosServiceLocator` (módulo `Logica`) las lee para localizar el EJB remoto de Datos:

| Variable | Valor por defecto | Significado |
|---|---|---|
| `DATOS_EJB_URL` | `remote+http://datos:8080` | Dirección del WildFly de datos dentro de la red de Docker |
| `DATOS_EJB_USER` | `datos-app` | Usuario de aplicación de datos (igual a `DATOS_APP_USER`) |
| `DATOS_EJB_PASSWORD` | `datos_pass` | Contraseña de aplicación de datos (igual a `DATOS_APP_PASSWORD`) |

---

## Ejecutar el cliente

El cliente (`Cliente/src/main/java/com/app/cliente/Principal.java`) busca por JNDI
`ejb:LogicaEAR/Logica/LogicaService!com.app.logica.interfaces.LogicaServiceRemote`
y ejecuta los 13 casos del taller, marcando cada uno como `[OK]` / `[ERROR ESPERADO]`
y mostrando un resumen al final. El entorno debe estar levantado (`docker compose up --build -d`).

### Opción A: con Docker (no necesitas Java)

```bash
docker compose run --rm cliente
```

El servicio `cliente` tiene un *profile*, así que **no** arranca con `docker compose up`;
solo con `run`. Se conecta a `remote+http://logica:8080` dentro de la red de Docker.

### Opción B: con Maven en tu máquina (Java 21 + Maven)

Desde la raíz del repositorio:

```bash
mvn install                       # compila, ejecuta las pruebas e instala los módulos en ~/.m2
mvn -pl Cliente exec:java         # ejecuta com.app.cliente.Principal
```

`mvn install` hace falta la primera vez (y cada vez que cambies DTOs o interfaces): sin
él, `-pl Cliente` no encuentra los módulos `DTOs` y `LogicaInterface`.

Variables opcionales del cliente:

| Variable | Valor por defecto |
|---|---|
| `LOGICA_EJB_URL` | `remote+http://localhost:8080` |
| `LOGICA_EJB_USER` | `logica-app` |
| `LOGICA_EJB_PASSWORD` | `logica_pass` |

> Cada ejecución registra 3 solicitudes nuevas. Para empezar desde cero:
> `docker compose down -v && docker compose up --build -d`.

---

## Archivos de Docker

| Archivo | Contenido |
|---|---|
| `docker-compose.yml` | Servicios `mysql`, `datos`, `logica` y `cliente` (este último con profile). |
| `docker/Dockerfile` | Multi-stage: etapa `build` (Maven + JDK 21) targets `logica` y `datos` (WildFly 40.0.1.Final) y `cliente` (JRE 21). |
| `docker/configure-datos.cli` | Registra el driver MySQL (módulo `com.mysql`), el datasource `MySQLDS` y `node-identifier=datos`. |
| `docker/configure-logica.cli` | `node-identifier=logica`. |
| `.env.example` | Todas las variables con sus valores por defecto. |
| `.dockerignore` | Excluye `target/`, `.git`, IDEs e instalaciones locales de WildFly del contexto de build. |

---

## Arquitectura

<!-- TODO: completar al final del taller (diagrama de los 3 niveles, EJB remotos
     entre logica y datos, direcciones JNDI ejb:EAR/módulo/Bean!interfaz, flujo de
     una solicitud desde el Cliente hasta MySQL). -->

_TODO: se completará al final del taller._
