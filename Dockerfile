# Coliseum - imagen del modo servicio/consola (la ventana Swing no puede ejecutarse dentro de un contenedor).
# Construcción en dos etapas: se compila con el JDK y se ejecuta con un JRE mínimo y usuario sin privilegios.

# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /build
COPY src/Coliseum/ ./
RUN mkdir /out \
 && javac -encoding UTF-8 -cp "lib/*" -d /out \
      $(find config excepciones modelo seguridad dao servicio consola -name '*.java')

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:17-jre
# Usuario distinto de root (hardening). UID numérico para que Kubernetes pueda verificarlo (runAsNonRoot).
RUN groupadd --system --gid 10001 coliseum \
 && useradd --system --uid 10001 --gid coliseum --no-create-home --shell /usr/sbin/nologin coliseum \
 && mkdir -p /app/classes /app/lib /app/logs \
 && chown -R 10001:10001 /app/logs
COPY --from=build /out /app/classes
COPY src/Coliseum/lib/mysql-connector-j-26.7.0.jar /app/lib/
WORKDIR /app/logs
USER 10001:10001
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Djava.awt.headless=true"
EXPOSE 8080
# Credenciales: NO están en la imagen; llegan por variables de entorno (DB_HOST, DB_USER, DB_PASSWORD...).
ENTRYPOINT ["java", "-cp", "/app/classes:/app/lib/*", "consola.ConsolaMain"]
CMD ["--servicio"]
