# Informe de seguridad (SSDLC)

Análisis previstos: **SCA**, **SAST** y **escaneo de imagen** (3 de los 4 pedidos). **DAST (OWASP ZAP) no aplica**: Coliseum es una aplicación de escritorio y no expone un sitio web que escanear; el único endpoint HTTP es `/health`, que no recibe datos.

> Estado: la sección 1 (revisión manual y correcciones) ya está hecha en el código. Las secciones 2, 3 y 4 describen cómo ejecutar cada herramienta y tienen espacio para **pegar los resultados reales** (los generan ustedes en el taller).

## 1. Hallazgos de la revisión manual y correcciones aplicadas

| # | Hallazgo (versión original) | Riesgo | Corrección |
|---|---|---|---|
| 1 | Usuario `root` y contraseña escritos en `Coliseum.java` y en el script SQL (`ALTER USER ... IDENTIFIED BY`) | Credenciales expuestas a cualquiera con acceso al repositorio | Credenciales en variables de entorno / `.env` (`config/Configuracion.java`); `.env` en `.gitignore`; el script SQL ya no toca usuarios |
| 2 | La contraseña de ese entorno sigue en el **historial de Git** | Aunque se borre del código, se puede recuperar de commits viejos | **Rotar** (cambiar) esa contraseña y no reutilizarla. Opcional: limpiar el historial con `git filter-repo` o BFG y hacer `git push --force` (coordinar con todo el grupo) |
| 3 | Contraseñas de usuarios con SHA-256 **sin sal** | Hashes rápidos de atacar con tablas precalculadas; dos usuarios con la misma clave tienen el mismo hash | PBKDF2-HMAC-SHA256, sal aleatoria de 16 bytes y 120.000 iteraciones (`seguridad/Pbkdf2Hash`). Las cuentas viejas se migran solas al iniciar sesión |
| 4 | Conexión con `useSSL=false` | Datos y contraseña viajan sin cifrar | `sslMode=REQUIRED` por defecto (`config/Conexion`); `DISABLED` solo para la base local de Docker |
| 5 | La aplicación creaba/alteraba tablas al arrancar (DDL con `Statement`) | Requiere permisos de administrador en la base; esquema fuera de control de versiones | Se eliminó. El esquema vive en `sql/01-crear-tablas.sql` |
| 6 | `printStackTrace()` y mensajes de excepción mostrados al usuario | Filtración de detalles internos | Excepciones propias con mensajes seguros; el detalle va a `coliseum.log` (`config/Registro`) |
| 7 | Sin separación de privilegios | Cualquier usuario podría gestionar a otros | Roles ADMIN/USUARIO; el rol se vuelve a leer de la base en cada operación administrativa (`UsuarioServicio.exigirAdmin`) |
| 8 | Contenedor sin usuario propio | Un fallo del proceso tendría privilegios de root | `Dockerfile` con usuario UID 10001; en Kubernetes `runAsNonRoot`, `allowPrivilegeEscalation: false`, `capabilities.drop: ALL` |
| 9 | Contraseñas posibles de 4 caracteres | Fáciles de adivinar | Mínimo 6 caracteres, máximo 72 (`Validador.contrasena`) |

Lo que se mantiene a propósito: `PreparedStatement` en todas las consultas (R9) y hash de contraseñas (R3), que se evalúan también como seguridad.

Límites conocidos (para explicar en la defensa): no hay bloqueo por intentos fallidos de inicio de sesión; la verificación de SSL es `REQUIRED` (cifra el canal pero no valida el certificado del servidor; `VERIFY_CA` exigiría importar el certificado CA de Aiven).

## 2. SCA: dependencias (Dependabot / Trivy)

- La única dependencia Java es `mysql-connector-j` (JAR en `src/Coliseum/lib/`). Dependabot no lee JARs sueltos, por eso `.github/dependabot.yml` vigila las **imágenes base de Docker** y las **GitHub Actions**; y Trivy (sección 4) analiza el JAR dentro de la imagen.
- Ejecución local del escaneo del repositorio:

```bash
docker run --rm -v "$PWD:/repo" aquasecurity/trivy:latest fs --scanners vuln,secret /repo
```

**Resultados (pegar aquí):**

```
(salida de Trivy fs / alertas de Dependabot)
```

**Hallazgos y decisión:** _(por cada vulnerabilidad: paquete, versión, gravedad, ¿afecta realmente a Coliseum?, acción: actualizar / aceptar con justificación)_

## 3. SAST: código fuente (SonarLint)

1. Instalar la extensión **SonarQube for IDE (SonarLint)** en VS Code.
2. Abrir `src/Coliseum/` y revisar el panel *Problems*; prestar atención a las reglas de seguridad (credenciales, SQL, criptografía).
3. Anotar los hallazgos relevantes y qué se corrigió.

**Resultados (pegar captura o lista):**

| Archivo | Regla / mensaje | Gravedad | Acción |
|---|---|---|---|
| | | | |

## 4. Escaneo de la imagen (Trivy)

El workflow `docker-publish.yml` ejecuta Trivy y guarda el resultado como artefacto `trivy-imagen`. También se puede correr a mano:

```bash
docker build -t coliseum-local:scan .
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasecurity/trivy:latest image --severity HIGH,CRITICAL coliseum-local:scan
```

**Resultados (pegar aquí):**

```
(salida de Trivy image)
```

**Hallazgos y decisión:** _(los de la imagen base `eclipse-temurin` suelen resolverse reconstruyendo con la etiqueta más nueva; anotar cuáles quedan y por qué)_

## 5. Lista de hardening mínimo

| Control de la consigna | Estado | Evidencia |
|---|---|---|
| Sin contraseñas en el código, el Dockerfile ni el historial de Git | Código y Dockerfile: sí. Historial: **pendiente** (hallazgo 2) | `grep -rniE "(password|clave|contrasena)\s*=\s*\"[^\"]+\"" src/ --include=*.java` no encuentra nada; ver hallazgo 2 |
| Contenedor con usuario distinto de root | Sí | `Dockerfile` (`USER 10001:10001`) y `k8s/app-deployment.yaml` |
| Conexión a Aiven solo por SSL | Sí | `DB_SSL_MODE=REQUIRED`; pantalla Estado muestra "SSL: sí" |
| R3: hash de contraseñas | Sí | `seguridad/Pbkdf2Hash` |
| R9: PreparedStatement | Sí | `dao/jdbc/*` (los únicos `Statement` importados son para `RETURN_GENERATED_KEYS` de un `PreparedStatement`) |
| `.env` fuera de Git y `.env.example` sin valores | Sí | `.gitignore`, `.env.example` |
| Imagen sin credenciales | Sí | `Dockerfile`: las variables llegan en tiempo de ejecución (compose o `Secret` de Kubernetes) |
| Credenciales de Kubernetes en `Secret`, no en `ConfigMap` | Sí | `k8s/secret.yaml.example`, `k8s/configmap.yaml` |

## 6. DAST (OWASP ZAP)

No aplica por ser un proyecto de escritorio (ver el inicio del informe). Si el profesor lo pide, se puede apuntar ZAP **solo** a `http://localhost:8080/health` de su propio contenedor, nunca a sitios ajenos.
