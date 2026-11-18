# Coliseum

## Descripción

Coliseum es una aplicación de escritorio desarrollada en Java como proyecto académico (Programación Avanzada, UTU).
Permite organizar rutinas de entrenamiento: calendario con notas por fecha, rutina semanal, reloj, cronómetro,
recordatorios con notificaciones y reportes. Los datos se guardan en MySQL (local con Docker o en la nube con Aiven).

## Funcionalidades

- Registro e inicio de sesión con roles (**Administrador** y **Usuario**).
- Calendario: alta, consulta, modificación y baja de notas por fecha.
- Rutina semanal con hora de entreno y recordatorios/notificaciones automáticas.
- Reportes y búsqueda con filtros; el administrador ve a todos los usuarios.
- Administración de usuarios (solo ADMIN): cambiar rol, restablecer contraseña, eliminar.
- Mi cuenta: cambiar contraseña y nombre, eliminar la cuenta.
- Pantalla **Estado** que verifica la conexión con la base de datos.
- Reloj y cronómetro.

## Tecnologías utilizadas

Java 11+ (probado con 17 y 21) · Java Swing · JDBC · MySQL 8 · MySQL Connector/J · Docker y docker-compose ·
GitHub Actions + GitHub Container Registry · Aiven (MySQL en la nube) · Kubernetes (minikube) · Trivy · Dependabot · SonarLint.

## Cómo ejecutarla (resumen)

1. `cp .env.example .env` y completar los valores (el archivo `.env` **no** se sube a Git).
2. **Base local en Docker:** `docker compose up --build` (crea las tablas y carga los datos de prueba).
3. **Ventana de escritorio (Windows):** doble clic en `src/Coliseum/Coliseum Launcher.bat`.
4. **Modo consola / servicio** (lo que corre en Docker y Kubernetes):
   `docker compose run --rm app --consola` o `GET http://localhost:8080/health`.

El paso a paso completo (Docker, Aiven, ghcr.io, Kubernetes) está en [`docs/guia-despliegue.md`](docs/guia-despliegue.md).
El informe de seguridad está en [`docs/informe-seguridad.md`](docs/informe-seguridad.md).

Otros documentos: [`docs/req.md`](docs/req.md) (requisitos), [`docs/stack.md`](docs/stack.md) (tecnologías),
[`docs/infraestructura.md`](docs/infraestructura.md) (entornos, variables y modelo de datos),
[`docs/uml_cdv.md`](docs/uml_cdv.md) (clases, capas y casos de uso) y [`docs/diagramas/`](docs/diagramas/).

## Estructura del repositorio

```
├── src/Coliseum/            código Java (ventana Swing + capas)
│   ├── config/              lectura de variables de entorno y conexión (R10)
│   ├── modelo/              entidades y jerarquía de herencia (R5)
│   ├── seguridad/           hash de contraseñas (interfaz + 2 implementaciones)
│   ├── dao/ y dao/jdbc/     acceso a datos: interfaces + implementación JDBC (R4, R9)
│   ├── servicio/            reglas de negocio, validaciones, transacción (R7, R8)
│   ├── excepciones/         excepciones propias (R8)
│   ├── consola/             modo sin ventana: consola, /health, servicio (R12)
│   └── *.java               presentación Swing (Coliseum.java y paneles)
├── sql/                     01-crear-tablas.sql · 02-datos-prueba.sql (R11)
├── Dockerfile · docker-compose.yml · .env.example · .dockerignore
├── k8s/                     manifiestos de Kubernetes (Nivel 3)
├── .github/                 Dependabot y publicación de la imagen en ghcr.io
└── docs/                    guía de despliegue, informe de seguridad, diagramas
```

## Tabla de requisitos (R1 a R12) y dónde están en el código

| Id | Requisito | Dónde está |
|----|-----------|-----------|
| R1 | Modelo de datos | `sql/01-crear-tablas.sql`: 4 tablas (`usuarios`, `rutina_semanal`, `rutinas_dia`, `avisos_entreno`) con PK y FK; tres relaciones 1:N desde `usuarios` (ver `docs/diagramas/modelo-datos.md`) |
| R2 | CRUD completo (3 entidades) | **usuarios:** `UsuarioServicio` (`registrar`, `buscarPorNombre`/`listar`, `cambiarContrasena`/`renombrar`, `eliminarCuenta`) · pantallas *Mi cuenta* y *Administración*. **rutinas_dia:** `RutinaServicio` (`agregarNota`, `listarNotas`, `editarNota`, `eliminarNota`) · diálogo del calendario (Agregar/Editar/Quitar). **rutina_semanal:** `RutinaServicio` (`guardarSemana`, `cargarSemana`, `eliminarDiaSemanal`) · diálogo de rutina semanal y opciones 7, 8 y 9 de la consola |
| R3 | Usuarios y roles | Login: `UsuarioServicio.autenticar`. Hash: `seguridad/Pbkdf2Hash` (PBKDF2-HMAC-SHA256 con sal). Roles `ADMIN` y `USUARIO`: `modelo/Rol`; permisos en `UsuarioServicio.exigirAdmin`, `ReporteServicio.alcance` y `AdminPanel` |
| R4 | Arquitectura en capas | Presentación (`*.java` Swing, `ConsolaMain`) → servicio (`servicio/`) → interfaces `dao/*DAO` → `dao/jdbc/*DAOJdbc`; modelo en `modelo/` |
| R5 | Orientación a objetos | Herencia: `Usuario` → `UsuarioComun` / `Administrador`. Interfaz con dos implementaciones: `HashContrasena` → `Pbkdf2Hash` / `Sha256Hash` (uso polimórfico en `Hashes.verificar`) |
| R6 | Consultas | `ReporteDAOJdbc.buscar` (filtros + JOIN), `resumenPorUsuario` (JOIN + COUNT + GROUP BY), `notasPorMes` (COUNT + GROUP BY), `promedioNotasPorUsuario` (AVG sobre subconsulta). Pantalla *Reportes* |
| R7 | Transacción | `RutinaServicio.guardarSemana`: escribe `rutina_semanal` y borra `avisos_entreno` con `setAutoCommit(false)`, `commit` y `rollback` |
| R8 | Validaciones y errores | `servicio/Validador` (todas las entradas); excepciones propias en `excepciones/` (`ColiseumException`, `ValidacionException`, `AutorizacionException`, `AccesoDatosException`); el detalle técnico va a `coliseum.log` y al usuario le llega un mensaje claro |
| R9 | Consultas seguras | Todas las consultas de `dao/jdbc/` y del motor de recordatorios usan `PreparedStatement` |
| R10 | Configuración externa | `config/Configuracion` y `config/Conexion` leen `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_SSL_MODE` del entorno (o de `.env`). No hay valores por defecto para usuario ni contraseña |
| R11 | Datos de prueba | `sql/01-crear-tablas.sql` y `sql/02-datos-prueba.sql` (49 registros) |
| R12 | Estado del servicio | Pantalla *Estado* (`EstadoPanel`, `SaludServicio`); en modo servicio, `GET /health` responde 200 si la app y la base funcionan (503 si no) y `ConsolaMain --health` devuelve el código de salida 0/1 |

## Niveles de la propuesta y dónde está la evidencia

Proyecto **de escritorio**: la ventana Swing corre en la PC; en Docker y Kubernetes corre el modo servicio (sin ventana) con `GET /health`. Todo con servicios gratuitos, sin tarjeta de crédito.

| Nivel | Pauta | Dónde está | Cómo se muestra |
|---|---|---|---|
| 1 (obligatorio) | `Dockerfile` de la aplicación | [`Dockerfile`](Dockerfile) (dos etapas, usuario no root) | `docker build .` |
| 1 | `docker-compose.yml` con app y base | [`docker-compose.yml`](docker-compose.yml) | `docker compose up --build` y `curl http://localhost:8080/health` |
| 1 | Credenciales fuera del código, en `.env` | [`config/Configuracion.java`](src/Coliseum/config/Configuracion.java), `.env` | La app lee `DB_*` del entorno |
| 1 | `.env` en `.gitignore` y `.env.example` sin valores | [`.gitignore`](.gitignore), [`.env.example`](.env.example) | `git check-ignore .env` |
| 1 | Imagen en GitHub Container Registry | [`.github/workflows/docker-publish.yml`](.github/workflows/docker-publish.yml) | Pestaña *Packages* de GitHub |
| 2 | MySQL en Aiven | [`docs/guia-despliegue.md`](docs/guia-despliegue.md), Nivel 2 | Consola de Aiven |
| 2 | Scripts de creación y carga contra la nube | [`sql/01-crear-tablas.sql`](sql/01-crear-tablas.sql), [`sql/02-datos-prueba.sql`](sql/02-datos-prueba.sql) | `mysql ... --ssl-mode=REQUIRED < sql/...` |
| 2 | Conexión por variables de entorno y SSL | [`config/Conexion.java`](src/Coliseum/config/Conexion.java) (`DB_SSL_MODE`) | Pantalla *Estado*: "conexión cifrada (SSL): sí" |
| 2 | Desktop: app conectada a Aiven, mismos datos desde dos máquinas | `Coliseum Launcher.bat` con `.env` apuntando a Aiven | Nota creada en una PC y vista en otra; `SELECT` directo sobre Aiven |
| 3 | `Deployment` (imagen, variables, `requests`/`limits`) y `Service` para app y base | [`k8s/app-deployment.yaml`](k8s/app-deployment.yaml), [`k8s/app-service.yaml`](k8s/app-service.yaml), [`k8s/db-deployment.yaml`](k8s/db-deployment.yaml), [`k8s/db-service.yaml`](k8s/db-service.yaml) | `kubectl get pods,svc` |
| 3 | Credenciales en `Secret`, configuración en `ConfigMap` | [`k8s/secret.yaml.example`](k8s/secret.yaml.example), [`k8s/configmap.yaml`](k8s/configmap.yaml) | `kubectl describe pod` |
| 3 | `livenessProbe` y `readinessProbe` | `/health` en la app; `mysqladmin ping` en el contenedor de la base | `kubectl get pods` con `READY 1/1` |

Análisis de seguridad (SCA, SAST y escaneo de imagen): [`docs/informe-seguridad.md`](docs/informe-seguridad.md).
Lista de control de entrega y defensa: [`docs/guia-despliegue.md`](docs/guia-despliegue.md#reproducibilidad-y-lista-de-control).

## Integrantes

- Facundo Gutiérrez.
- Lucas Peraza.
- Lucas Jaques.
- Nahuel Larrauri.
