# Guía de despliegue de Coliseum

Proyecto **de escritorio**: la ventana Swing corre en la PC y se conecta a la base. Para Docker y Kubernetes se usa el
**modo servicio** (sin ventana), que verifica la conexión con la base y expone `GET /health`.
Cada parte tiene un espacio para la captura obligatoria (`📸`).

## 0. Requisitos previos

- Git, Docker Desktop (incluye `docker compose`), `kubectl` y `minikube` (o `kind`).
- Java 11 o superior (solo para la ventana de escritorio).
- Cuentas gratuitas, sin tarjeta: GitHub y Aiven.

```bash
git clone https://github.com/RodillaOff/proyecto_JBDPA_grupo_3.git
cd proyecto_JBDPA_grupo_3
cp .env.example .env        # en Windows: copy .env.example .env
```

Completá `.env` (el archivo no se sube a Git). Para el Nivel 1 (base local en Docker) un ejemplo de valores:

| Variable | Ejemplo para Docker local |
|---|---|
| `DB_HOST` | `localhost` (solo para la ventana; el contenedor usa `db`) |
| `DB_PORT` | `5433` (puerto publicado por compose) |
| `DB_NAME` | `coliseum_db` |
| `DB_USER` | `coliseum` |
| `DB_PASSWORD` | una contraseña que elijas |
| `DB_SSL_MODE` | `DISABLED` |

## Nivel 1. Contenerizar

### 1.1 Levantar todo con un comando

```bash
docker compose up --build
```

La primera vez, PostgreSQL ejecuta solos `sql/01-crear-tablas.sql` y `sql/02-datos-prueba.sql`. Esperá a que `coliseum-app` quede `healthy`.

```bash
docker compose ps
curl http://localhost:8080/health          # responde OK (200)
```
![Descripción de la captura](<img width="1579" height="157" alt="1" src="https://github.com/user-attachments/assets/eee3f15f-195e-4379-8de7-12703f3c461b" />
)<img width="1579" height="157" alt="1" src="https://github.com/user-attachments/assets/e34bf50b-0bc0-4fcc-997a-a75b9cdad0fc" />


### 1.2 Comprobar que la app guarda y lee datos de la base del contenedor

```bash
docker compose run --rm app --consola
# 1) Iniciar sesión → usuario: ana   contraseña: Prueba#2026
# 2) Agregar nota → fecha 2026-11-20 → "Prueba desde Docker"
# 1) Ver notas de esa fecha → debe aparecer
docker compose exec db psql -U coliseum -d coliseum_db -c "SELECT id, usuario_id, fecha, descripcion FROM rutinas_dia ORDER BY id DESC LIMIT 5;"
```

> Cuentas de prueba (solo para demostración): `admin` / `Admin#2026` (rol ADMIN); `ana`, `bruno`, `carla`, `diego` / `Prueba#2026` (rol USUARIO).
> Están cargadas como hash en `sql/02-datos-prueba.sql`. En un entorno real se cambian.

📸 ![Descripción de la captura](<img width="1600" height="710" alt="11" src="https://github.com/user-attachments/assets/bb8a5726-8223-470e-a3a1-7cd95dcd3271" />
) `docker compose up` funcionando y la nota guardada/consultada.

### 1.3 Publicar la imagen en GitHub Container Registry

Opción automática (recomendada): al hacer `push` a `main`, el workflow `.github/workflows/docker-publish.yml` construye, escanea con Trivy y publica `ghcr.io/<tu-usuario-en-minúsculas>/coliseum:latest`. No necesita secretos.

1. En GitHub: *Settings → Actions → General → Workflow permissions* debe permitir escritura (el workflow ya pide `packages: write`).
2. Después del primer push: pestaña *Packages* del perfil/organización → `coliseum` → *Package settings* → visibilidad **Public** (así Kubernetes puede bajarla sin credenciales).

Opción manual:

```bash
echo <TOKEN_con_write:packages> | docker login ghcr.io -u <usuario> --password-stdin
docker build -t ghcr.io/<usuario>/coliseum:latest .
docker push ghcr.io/<usuario>/coliseum:latest
```

📸 ![Descripción de la captura](<img width="764" height="264" alt="22" src="https://github.com/user-attachments/assets/63f8225e-1899-477f-9885-b488d045c9fb" />
)
## Nivel 2. Llevarlo a la nube (Aiven, PostgreSQL)

> **Por qué PostgreSQL:** desde el 6/11/2026 Aiven ya no permite crear bases **MySQL** gratuitas
> (solo con plan pago). El plan **Free de PostgreSQL** sigue disponible y sin tarjeta, y la consigna
> admite "Aiven (MySQL o PostgreSQL)". Por eso el proyecto usa PostgreSQL en todos los niveles.

1. Entrá a <https://console.aiven.io>, creá la cuenta y un servicio **PostgreSQL** con el plan **Free**
   (nombre `coliseum`). El plan gratis permite un solo PostgreSQL por organización.
2. Cuando esté en *Running*, copiá de *Overview → Connection information*: *Host*, *Port*, *User*
   (`avnadmin`), *Password* y *Database name* (`defaultdb`). En *CA certificate* tocá **Download** y
   guardá el archivo como `ca.pem` en la raíz del proyecto (está en `.gitignore`).
3. Cargá los scripts contra Aiven. Hay tres formas:
   - **Sin instalar nada:** en la consola de Aiven, *SQL editor*: pegá el contenido de
     `sql/01-crear-tablas.sql`, **Run**; después el de `sql/02-datos-prueba.sql`, **Run**.
   - **Con `psql` instalado:**
     ```bash
     psql "host=<HOST> port=<PUERTO> dbname=defaultdb user=avnadmin sslmode=verify-full sslrootcert=ca.pem" -f sql/01-crear-tablas.sql
     psql "host=<HOST> port=<PUERTO> dbname=defaultdb user=avnadmin sslmode=verify-full sslrootcert=ca.pem" -f sql/02-datos-prueba.sql
     ```
   - **Con Docker:**
     ```bash
     docker run --rm -it -v "$PWD":/w -w /w postgres:16-alpine psql "host=<HOST> port=<PUERTO> dbname=defaultdb user=avnadmin sslmode=verify-full sslrootcert=ca.pem" -f sql/01-crear-tablas.sql
     ```

4. Apuntá la aplicación a Aiven editando **solo** tu `.env` local:

```
DB_HOST=<host de Aiven>
DB_PORT=<puerto de Aiven>
DB_NAME=defaultdb
DB_USER=avnadmin
DB_PASSWORD=<contraseña de Aiven>
DB_SSL_MODE=VERIFY_IDENTITY
DB_SSL_ROOT_CERT=C:\ruta\al\proyecto\ca.pem
```

`VERIFY_IDENTITY` cifra la conexión **y** comprueba con `ca.pem` que el servidor es realmente el de
Aiven. Con `REQUIRED` solo cifra (también funciona, sin `DB_SSL_ROOT_CERT`).

5. Abrí la ventana (`src/Coliseum/Coliseum Launcher.bat`), iniciá sesión, entrá a **Estado** → *Verificar conexión*: debe decir *conexión cifrada (SSL): sí*. Creá una nota en el calendario.
6. Comprobá con un SELECT directo sobre Aiven (en el *SQL editor* de la consola, o con `psql`):

```sql
SELECT r.id, u.nombre_usuario, r.fecha, r.descripcion
FROM rutinas_dia r JOIN usuarios u ON u.id = r.usuario_id
ORDER BY r.id DESC LIMIT 5;
```

7. Repetí los pasos 4 y 5 en **otra PC** (con su propio `.env` y el mismo `ca.pem`): debe verse la misma nota.

> Aiven apaga los servicios Free sin uso: si no conecta, encendelo desde la consola (*Power on*) antes de la defensa.

📸 ![Descripción de la captura](<img width="927" height="716" alt="4" src="https://github.com/user-attachments/assets/e5f3a90b-ea19-4f03-8fcb-033d9308ba52" />
)
![Descripción de la captura](<img width="886" height="197" alt="3" src="https://github.com/user-attachments/assets/00fa167e-9fe1-48ec-b391-a1ad0cc7dfa2" />
)

(Render no aplica: el proyecto es de escritorio.)

## Nivel 3. Kubernetes local (minikube)

```bash
minikube start
kubectl create configmap coliseum-sql --from-file=sql/
kubectl create secret generic coliseum-secret \
  --from-literal=DB_USER=coliseum \
  --from-literal=DB_PASSWORD='<contraseña>'
kubectl apply -f k8s/configmap.yaml -f k8s/db-deployment.yaml -f k8s/db-service.yaml
kubectl apply -f k8s/app-deployment.yaml -f k8s/app-service.yaml
kubectl get pods,svc          # esperar a que todo esté Running / READY 1/1
```

Antes, en `k8s/app-deployment.yaml` ajustá `image:` con tu usuario de GitHub en minúsculas. Si la imagen es privada o todavía no está publicada, cargala directo en minikube:

```bash
docker build -t ghcr.io/<usuario>/coliseum:latest .
minikube image load ghcr.io/<usuario>/coliseum:latest
```

Comprobar la app en el clúster:

```bash
minikube service coliseum-app --url        # devuelve la URL; abrir <URL>/health  → OK
kubectl logs deploy/coliseum-app
```

Qué hay en los manifiestos:

- `configmap.yaml`: configuración no secreta (host, puerto, nombre de la base).
- `secret.yaml.example`: plantilla del Secret (usuario y contraseñas); el real se crea con `kubectl create secret` y no se versiona.
- `db-deployment.yaml` / `db-service.yaml`: PostgreSQL con volumen persistente, `requests/limits` y probes `pg_isready` / `SELECT 1`.
- `app-deployment.yaml` / `app-service.yaml`: la app en modo servicio, con `readinessProbe` y `livenessProbe` sobre `/health`, `requests/limits`, usuario no root y sin privilegios extra.

📸 **Captura 4:** `kubectl get pods,svc` con todo en Running y `/health` respondiendo OK.

> Evidencia real (ejecutada con `scripts/captura4-kubernetes.bat`): [`docs/evidencias/captura4-resumen.md`](evidencias/captura4-resumen.md), `n3-get-pods-svc.txt` (pods `Running 1/1`) y `n3-health-en-cluster.txt` (`/health` OK).

## Solución de problemas

| Síntoma | Causa y solución |
|---|---|
| `Falta la variable de entorno DB_HOST` | No existe `.env` o está vacío. Copiá `.env.example` y completalo. |
| La app no conecta a Aiven | Servicio apagado (encenderlo), `DB_SSL_MODE` en `DISABLED` (Aiven exige SSL), `ca.pem` en otra ruta que `DB_SSL_ROOT_CERT`, o puerto/host mal copiados. |
| `docker compose up` falla en `db` | Faltan `DB_USER`, `DB_PASSWORD` o `DB_NAME` en `.env`. Si cambiaste credenciales después de la primera vez: `docker compose down -v`. |
| El pod `coliseum-app` queda `ImagePullBackOff` | La imagen no existe o es privada: publicala/hacela pública o usá `minikube image load`. |
| Pod `coliseum-app` en `0/1` | La base todavía no responde (se espera) o el Secret no existe (`kubectl describe pod`). |
| Base local creada con una versión vieja del proyecto | Esta versión agrega la columna `usuarios.rol`. Borrá la base vieja y volvé a ejecutar los dos scripts de `sql/`. |

## Reproducibilidad y lista de control

La consigna pide un despliegue reproducible a partir de la documentación. Esta guía y el `README.md` deben alcanzar para que otra persona lo repita sin ayuda; las correcciones que surjan al probarla se anotan en el último apartado.

### Entrega

- [ ] Repositorio en GitHub con acceso para el docente (agregarlo como colaborador o hacer el repositorio público).
- [ ] `README.md` con la tabla de requisitos R1 a R12 y los archivos donde está cada uno.
- [ ] `.env` **no** está en el repositorio; `.env.example` sí, solo con los nombres de las variables.
- [ ] Ninguna contraseña en el código, en el `Dockerfile`, en `docker-compose.yml` ni en los manifiestos de `k8s/`.

### Nivel 1 (obligatorio)

- [ ] `Dockerfile` de la aplicación.
- [ ] `docker-compose.yml` con la app y la base.
- [ ] Credenciales en `.env`, con `.env` en `.gitignore`.
- [ ] Imagen publicada en GitHub Container Registry.
- [ ] Evidencia: `docker compose up` levanta todo con un solo comando y la app guarda y lee datos de la base del contenedor (captura 1 y 2).

### Nivel 2

- [ ] Base PostgreSQL en Aiven (plan gratuito, sin tarjeta).
- [ ] `01-crear-tablas.sql` y `02-datos-prueba.sql` ejecutados contra Aiven.
- [ ] Conexión por variables de entorno (host, puerto, base, usuario, contraseña) y SSL (`DB_SSL_MODE=REQUIRED`).
- [ ] La app (de escritorio) conectada a Aiven; los mismos datos se ven desde dos máquinas distintas.
- [ ] Evidencia: datos creados desde la app y consultados con un `SELECT` directo sobre Aiven (captura 3).

### Nivel 3

- [ ] `minikube` o `kind` funcionando en la máquina del taller.
- [ ] `Deployment` con imagen, variables de entorno, `requests` y `limits` (app y base); `Service` (app y base).
- [ ] Credenciales en un `Secret` y configuración (puertos, URLs) en un `ConfigMap`.
- [ ] `readinessProbe` y `livenessProbe` (sobre `/health` en la app y con `pg_isready` en la base).
- [ ] Evidencia: `kubectl get pods,svc` con todo en `Running` y la app funcionando en el clúster (captura 4).

### Seguridad

- [ ] Informe de seguridad con SCA, SAST y escaneo de imagen **con los resultados reales pegados** (`docs/informe-seguridad.md`, secciones 2 a 4).
- [ ] Hallazgo 2 resuelto: contraseña antigua rotada (y, si se decide, historial de Git limpiado).

### Defensa (oral, en grupo, con prueba en vivo)

- [ ] Cada integrante puede explicar cualquier parte del proyecto, incluidos `Dockerfile`, `docker-compose.yml`, `.env.example`, los manifiestos de `k8s/` y el workflow de GitHub Actions.
- [ ] La evidencia de cada requisito está lista para mostrarse (clase, método o tabla): ver la tabla del `README.md`.
- [ ] Aiven está encendido el día de la defensa (los servicios gratuitos se apagan por inactividad).
- [ ] Todo el trabajo usa servicios gratuitos y no se ingresaron datos de tarjeta. Si un integrante no puede crear una cuenta (edad o términos del servicio), se usa la de otro integrante o se resuelve en el taller.
- [ ] Uso de IA: solo para consultar conceptos y resolver errores; cada integrante puede explicar lo que se entrega.

### Correcciones surgidas al probar la guía (Taller 6)

| Fecha | Quién probó | Qué falló o faltaba | Corrección |
|---|---|---|---|
| | | | |
