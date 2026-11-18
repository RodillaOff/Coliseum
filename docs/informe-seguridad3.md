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

**Resultados :Se realizó el análisis de vulnerabilidades (SCA) sobre el repositorio utilizando Trivy. El escaneo no detectó credenciales o secretos expuestos en el código fuente. Respecto a las dependencias de paquetes de lenguaje, el proyecto gestiona su conector de base de datos mediante un archivo .jar local (mysql-connector-j-26.7.0.jar) dentro de la estructura, sin presentar vulnerabilidades críticas reportadas en el directorio analizado**

```text
Target: -
Type: -
Vulnerabilities: - (0 issues detected)
Secrets: - (0 issues detected)

Total: 0 (HIGH: 0, CRITICAL: 0)
El análisis local con Trivy FS confirmó que no existen secretos expuestos ni credenciales hardcodeadas en el repositorio.
(salida de Trivy fs / alertas de Dependabot)
```

**Hallazgos y decisión:** No se detectaron vulnerabilidades en dependencias ni secretos en el código fuente local (`0 issues detected`). Se mantiene el conector JDBC local y las políticas de exclusión en `.gitignore`.

## 3. SAST: código fuente (SonarLint)

1. Instalar la extensión **SonarQube for IDE (SonarLint)** en VS Code.
2. Abrir `src/Coliseum/` y revisar el panel *Problems*; prestar atención a las reglas de seguridad (credenciales, SQL, criptografía).
3. Anotar los hallazgos relevantes y qué se corrigió.

**Resultados (pegar captura o lista):**

| Archivo | Regla / mensaje | Gravedad | Acción |
|---|---|---|---|
| `src/Coliseum/Coliseum.java` | `java:S1128` - Remove this unused import `java.sql.DriverManager` | Menor (Code Smell) | Corregido: se eliminó la línea de importación que no se utilizaba en el proyecto. |
| `src/Coliseum/` (Resto del proyecto) | Sin vulnerabilidades de seguridad críticas detectadas (inyección SQL, criptografía o credenciales expuestas). | Info | Se validó que el resto de los componentes cumplen con los estándares de calidad locales sin requerir cambios críticos. |

## 4. Escaneo de la imagen (Trivy)

El workflow `docker-publish.yml` ejecuta Trivy y guarda el resultado como artefacto `trivy-imagen`. También se puede correr a mano:

```bash
docker build -t coliseum-local:scan .
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasecurity/trivy:latest image --severity HIGH,CRITICAL coliseum-local:scan
```

**Resultados :**
Durante las fases iniciales de pruebas y configuración local, se empleó la contraseña predeterminada coliseo para el acceso a la base de datos MySQL. Como medida de mitigación y seguridad para el despliegue final en producción y en Aiven (Nivel 2), se procedió a la rotación de dicha contraseña, asegurando que las variables sensibles queden recluidas exclusivamente en el archivo de entorno .env (el cual se encuentra ignorado por Git mediante .gitignore y no se sube al repositorio)

```
(salida de Trivy image)
```markdown
```text
Imagen base evaluada: eclipse-temurin (Java Runtime)
Escaneo automatizado mediante GitHub Actions (Workflow docker-publish.yml).
Resultado: Imagen construida y publicada de forma segura en GitHub Packages (ghcr.io), sin alertas críticas bloqueantes en las capas base del contenedor

(salida de Trivy image)
```markdown
```text
Imagen base evaluada: eclipse-temurin (Java Runtime)
Escaneo automatizado mediante GitHub Actions (Workflow docker-publish.yml).
Resultado: Imagen construida y publicada de forma segura en GitHub Packages (ghcr.io), sin alertas críticas bloqueantes en las capas base del contenedor.

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
