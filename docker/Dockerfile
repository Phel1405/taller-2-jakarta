# syntax=docker/dockerfile:1
#
# Dockerfile multi-stage. Se construye desde la RAÍZ del repositorio:
#   docker compose up --build            (recomendado)
#   docker build -f docker/Dockerfile --target logica .
#   docker build -f docker/Dockerfile --target datos  .
#   docker build -f docker/Dockerfile --target cliente .

# ---------------------------------------------------------------------------
# Etapa build: compila todo el proyecto Maven y genera los dos EAR.
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /src
COPY . .

# La caché de BuildKit conserva ~/.m2 entre builds: las dependencias solo se
# descargan la primera vez. copy-dependencies deja en Cliente/target/dependency
# las librerías que necesita el cliente para ejecutarse (target "cliente").
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp package dependency:copy-dependencies -DincludeScope=runtime -DskipTests

# ---------------------------------------------------------------------------
# Base común de WildFly
# ---------------------------------------------------------------------------
FROM quay.io/wildfly/wildfly:40.0.1.Final-jdk21 AS wildfly-base

USER jboss

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-full.xml"]

# ---------------------------------------------------------------------------
# Target logica: tier de negocio (puerto 8080 en el host).
# ---------------------------------------------------------------------------
FROM wildfly-base AS logica

# Usuario de aplicación (ApplicationRealm) para invocar los EJB remotos de
# Logica (por ejemplo, desde el Cliente en el host).
ARG APP_USER=logica-app
ARG APP_PASSWORD=logica_pass
RUN /opt/jboss/wildfly/bin/add-user.sh -a -u "${APP_USER}" -p "${APP_PASSWORD}" --silent

COPY --chown=jboss:jboss docker/configure-logica.cli /tmp/configure-logica.cli
# Se borra el historial que deja embed-server: si queda en una capa de la imagen,
# WildFly no puede renombrarlo al arrancar (WFLYCTL0056).
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure-logica.cli \
    && rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history

COPY --from=build --chown=jboss:jboss /src/LogicaEAR/target/LogicaEAR.ear /opt/jboss/wildfly/standalone/deployments/

# ---------------------------------------------------------------------------
# Target datos: tier de datos con JPA + MySQL (puerto 8090 en el host).
# ---------------------------------------------------------------------------
FROM wildfly-base AS datos

ARG MYSQL_DRIVER_VERSION=9.1.0

USER root
RUN mkdir -p /tmp/drivers \
    && curl -fsSL -o /tmp/drivers/mysql-connector-j.jar \
        "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${MYSQL_DRIVER_VERSION}/mysql-connector-j-${MYSQL_DRIVER_VERSION}.jar" \
    && chown -R jboss:jboss /tmp/drivers
USER jboss

# Usuario de aplicación (ApplicationRealm) que usará Logica para invocar los
# EJB remotos de Datos.
ARG APP_USER=datos-app
ARG APP_PASSWORD=datos_pass
RUN /opt/jboss/wildfly/bin/add-user.sh -a -u "${APP_USER}" -p "${APP_PASSWORD}" --silent

# Registra el driver como módulo y crea el datasource MySQLDS directamente en
# standalone-full.xml (embed-server: no necesita un servidor en ejecución).
COPY --chown=jboss:jboss docker/configure-datos.cli /tmp/configure-datos.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure-datos.cli \
    && rm -rf /tmp/drivers /opt/jboss/wildfly/standalone/configuration/standalone_xml_history

COPY --from=build --chown=jboss:jboss /src/DatosEAR/target/DatosEAR.ear /opt/jboss/wildfly/standalone/deployments/

# ---------------------------------------------------------------------------
# Target cliente: cliente de consola que invoca los EJB de Logica.
# Se ejecuta con: docker compose run --rm cliente
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre AS cliente

WORKDIR /app
COPY --from=build /src/Cliente/target/dependency/ /app/lib/
COPY --from=build /src/Cliente/target/Cliente-*.jar /app/lib/

CMD ["java", "-cp", "/app/lib/*", "com.app.cliente.Principal"]
