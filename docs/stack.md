# Stack tecnológico

Proyecto **de escritorio** (Programación Avanzada, UTU). Todo el stack usa servicios gratuitos y no requiere tarjeta de crédito.

## Aplicación

- **Lenguaje:** Java 11 o superior (probado con 17 y 21). Programación orientada a objetos.
- **Interfaz gráfica:** Java Swing y AWT (`JFrame`, `JDialog`, `JPanel`, `CardLayout`, `GridBagLayout`).
- **Modo sin ventana (servicio/consola):** `consola.ConsolaMain` con `com.sun.net.httpserver.HttpServer` para `GET /health`. Es lo que corre dentro de Docker y Kubernetes.
- **Fechas y horas:** Java Time API (`LocalDate`, `LocalTime`, `YearMonth`, `DayOfWeek`).
- **Notificaciones y segundo plano:** toast de Windows con PowerShell, `SystemTray`/`TrayIcon`, `ScheduledExecutorService` (revisión cada 30 s), `ServerSocket` + `FileLock` (instancia única).

## Datos

- **Base de datos:** PostgreSQL 16 (contenedor `postgres:16-alpine` en local; servicio PostgreSQL gratuito de **Aiven** en la nube). Se cambió desde MySQL porque Aiven dejó de ofrecer MySQL gratis el 6/11/2026.
- **Acceso:** JDBC con el **driver de PostgreSQL** (`lib/postgresql-42.7.2.jar`). Todas las consultas usan `PreparedStatement` (R9).
- **Esquema y datos:** `sql/01-crear-tablas.sql` y `sql/02-datos-prueba.sql` (R11). La aplicación **no** crea ni altera tablas.

## Seguridad

- **Contraseñas:** PBKDF2-HMAC-SHA256 con sal aleatoria y 120.000 iteraciones (`seguridad/Pbkdf2Hash`). `Sha256Hash` queda solo para verificar cuentas antiguas, que se migran al iniciar sesión.
- **Roles:** `ADMIN` y `USUARIO` (`modelo/Rol`), comprobados en la capa de servicio.
- **Credenciales:** variables de entorno / archivo `.env` fuera de Git (R10).
- **Transporte:** SSL (`DB_SSL_MODE=REQUIRED`) contra Aiven.
- **Análisis (SSDLC):** SCA con Dependabot y Trivy, SAST con SonarLint, escaneo de imagen con Trivy (ver `informe-seguridad.md`).

## Contenedores y nube

- **Docker:** `Dockerfile` en dos etapas (JDK para compilar, JRE para ejecutar, usuario no root UID 10001).
- **docker compose:** `docker-compose.yml` con la app y PostgreSQL; levanta todo con `docker compose up`.
- **Registro de imágenes:** GitHub Container Registry (`ghcr.io`), publicada con GitHub Actions (`.github/workflows/docker-publish.yml`).
- **Nube:** Aiven (PostgreSQL). Render no aplica porque el proyecto es de escritorio.
- **Orquestación:** Kubernetes local con minikube (o kind): `Deployment`, `Service`, `ConfigMap`, `Secret`, `PersistentVolumeClaim`, `requests/limits`, `readinessProbe` y `livenessProbe` (`k8s/`).

## Herramientas de desarrollo

Git y GitHub, IntelliJ IDEA / VS Code, SonarLint, el SQL editor de Aiven, pgAdmin/DBeaver o `psql` para los SELECT de comprobación, Mermaid para los diagramas.
