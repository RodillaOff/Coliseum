# Requerimientos - Coliseum

## Requisitos funcionales (RF)

### 1. Acceso de usuarios
- **RF-01 (Registro de usuarios):** el sistema debe permitir registrar un nuevo usuario con nombre de usuario (3 a 30 caracteres: letras, números, punto, guion y guion bajo) y contraseña (6 a 72 caracteres) con confirmación. Todo usuario nuevo recibe el rol `USUARIO`.
- **RF-02 (Validación de duplicados):** el sistema debe verificar que el nombre de usuario no exista en la base de datos antes de registrarlo.
- **RF-03 (Inicio de sesión):** el sistema debe validar las credenciales comparando el hash de la contraseña ingresada con el almacenado en la base de datos.
- **RF-04 (Control de errores):** el sistema debe mostrar mensajes claros ante credenciales incorrectas, contraseñas que no coinciden, campos incompletos, datos demasiado cortos o fallos de conexión con la base de datos.
- **RF-05 (Acceso a la aplicación):** el sistema solo debe abrir la aplicación principal después de un inicio de sesión exitoso.

### 2. Inicio y navegación
- **RF-06 (Frases motivacionales):** el sistema debe mostrar una frase motivacional en la pantalla de inicio y permitir pedir una nueva. Las mismas frases se usan en las notificaciones de entreno.
- **RF-07 (Navegación):** el sistema debe ofrecer una barra lateral para cambiar entre Inicio, Calendario, Reloj, Cronómetro, Notificaciones, Reportes, Mi cuenta y Estado. La opción Administración solo aparece para el rol `ADMIN`.

### 3. Rutinas semanales y calendario
- **RF-08 (Resumen semanal):** el sistema debe permitir consultar y guardar un resumen corto de rutina para cada día de la semana. Al entrar al calendario por primera vez, sin rutina cargada, se ofrece configurarla.
- **RF-09 (Bloc de ejercicios diarios):** el sistema debe permitir cargar y guardar un bloc de notas con los ejercicios de cada día de la semana.
- **RF-10 (Notas por fecha, CRUD):** el sistema debe permitir agregar, consultar, modificar y eliminar notas de rutina para una fecha concreta del calendario.
- **RF-11 (Días con rutina):** el sistema debe marcar en el calendario las fechas del mes que tienen al menos una rutina registrada.

### 4. Herramientas de entrenamiento
- **RF-12 (Reloj):** el sistema debe mostrar la hora y la fecha actuales, actualizadas cada segundo.
- **RF-13 (Cronómetro):** el sistema debe ofrecer un cronómetro con formato HH:mm:ss.S (horas, minutos, segundos y décimas).
- **RF-14 (Control del cronómetro):** el cronómetro debe permitir iniciar, pausar (manteniendo el tiempo acumulado) y reiniciar a cero.

### 5. Notificaciones de entreno
- **RF-15 (Hora de inicio por día):** el sistema debe permitir guardar, junto al resumen de cada día de la semana, la hora de inicio del entreno en formato HH:mm. El campo solo acepta números, agrega el ":" automáticamente, rechaza horas o minutos inválidos y, si se deja vacío, el día queda sin hora propia (usa la hora por defecto).
- **RF-16 (Activar y desactivar avisos):** el sistema debe ofrecer una pantalla «Notificaciones» donde cada usuario active o desactive sus avisos de entreno. La preferencia se guarda por usuario y puede estar sin elegir.
- **RF-17 (Oferta inicial):** al abrir la aplicación, si el usuario todavía no eligió, el sistema debe ofrecerle activar las notificaciones («Activar ahora» / «Más tarde»).
- **RF-18 (Aviso previo al entreno):** el sistema debe mostrar una notificación de escritorio unos minutos antes de la hora del entreno (20 por defecto), con el título «Tu entreno comienza en N minutos» y una frase motivacional. Solo se avisa en los días que tienen entreno: un día vacío o que diga Descanso, Libre, Off o No entreno no genera aviso. También cuentan las rutinas cargadas para una fecha concreta.
- **RF-19 (Sin avisos repetidos):** el sistema debe registrar los avisos ya enviados para no repetir la misma notificación el mismo día. Si se cambia una hora en la rutina semanal, el aviso vuelve a estar disponible.
- **RF-20 (Aviso tardío):** si la aplicación estaba cerrada a la hora programada, el sistema debe avisar igual dentro de una ventana de tolerancia (30 minutos por defecto), pero nunca una vez que el entreno ya empezó.
- **RF-21 (Prueba y ajustes de Windows):** el sistema debe permitir enviar una notificación de prueba, detectar si las notificaciones están desactivadas en Windows y abrir la configuración de Windows con las instrucciones para activarlas.
- **RF-22 (Configuración externa):** el sistema debe leer, desde el archivo opcional `coliseum.properties`, la hora por defecto (`recordatorio.hora_por_defecto`), los minutos de anticipación (`recordatorio.minutos_antes`) y la ventana de tolerancia (`recordatorio.ventana_minutos`). Los valores se releen sin reiniciar la aplicación.

### 6. Segundo plano y ventana
- **RF-23 (Segundo plano):** al cerrar la ventana con la X, la aplicación no debe terminar: se esconde en la bandeja del sistema y sigue avisando de los entrenos. Solo se cierra del todo con «Salir» en el icono de la bandeja.
- **RF-24 (Icono de bandeja):** el sistema debe mostrar un icono en la bandeja del sistema con las opciones para abrir la ventana y salir. Si el sistema no tiene bandeja, al cerrar se lanza el servicio de avisos como proceso independiente.
- **RF-25 (Instancia única):** el sistema debe permitir una sola instancia por usuario del sistema operativo. Si se abre de nuevo, la instancia existente muestra su ventana y el nuevo proceso termina sin pedir login otra vez.
- **RF-26 (Modos sin ventana):** el sistema debe ofrecer el modo `--recordatorios` (servicio de avisos para todos los usuarios, sin ventana) y el modo `--simular "AAAA-MM-DD HH:mm"` (muestra a quién se avisaría en ese momento, sin enviar nada).
- **RF-27 (Logo):** el sistema debe usar el logo de la aplicación (`logo.png`, si existe) en la ventana, el login, la barra de tareas y la bandeja.

### 7. Roles, reportes, cuenta y estado (requisitos de la consigna R2, R3, R6, R7, R8, R12)
- **RF-28 (Roles):** el sistema debe distinguir dos roles con permisos distintos: `USUARIO` (gestiona solo sus propios datos) y `ADMIN` (además ve los datos de todos y administra usuarios). El permiso se vuelve a comprobar en la capa de servicio, no solo en la pantalla.
- **RF-29 (Administración de usuarios):** el administrador debe poder listar usuarios, cambiar el rol, restablecer la contraseña y eliminar usuarios. Un usuario común no accede a estas operaciones.
- **RF-30 (Mi cuenta):** cada usuario debe poder cambiar su contraseña (confirmando la actual), cambiar su nombre y eliminar su propia cuenta.
- **RF-31 (Reportes y búsqueda):** el sistema debe ofrecer una pantalla *Reportes* con búsqueda de notas por texto y rango de fechas, un resumen por usuario (cantidad de notas, `JOIN` + `COUNT` + `GROUP BY`), notas por mes y el promedio de notas por usuario (`AVG`). El usuario común ve solo lo suyo; el administrador ve a todos.
- **RF-32 (Guardado atómico de la semana):** al guardar la rutina semanal, el sistema debe escribir `rutina_semanal` y limpiar `avisos_entreno` en una sola transacción: si algo falla se hace `rollback` y no queda nada a medias.
- **RF-33 (Validación de entradas):** toda entrada (usuario, contraseña, textos, horas, fechas y rangos) debe validarse en `servicio/Validador` antes de llegar a la base, con mensajes claros para el usuario.
- **RF-34 (Estado del servicio):** la pantalla *Estado* debe verificar la conexión con la base y mostrar el resultado (incluido si la conexión es cifrada). En modo servicio, `GET /health` responde 200 si la app y la base funcionan y 503 si no; `--health` termina con código 0 o 1.
- **RF-35 (Modo consola):** el sistema debe ofrecer un modo de texto (`--consola`) con inicio de sesión, alta y consulta de notas, reporte y estado, para usarlo dentro de un contenedor.

## Requisitos no funcionales (RNF)

### 1. Seguridad
- **RNF-01 (Contraseñas con hash):** las contraseñas nunca se guardan en texto plano; se almacenan con PBKDF2-HMAC-SHA256, sal aleatoria de 16 bytes y 120.000 iteraciones (`seguridad/Pbkdf2Hash`). Las cuentas antiguas con SHA-256 se migran solas al iniciar sesión (`Sha256Hash`, `Hashes.verificar`).
- **RNF-02 (Consultas seguras):** toda consulta SQL debe usar `PreparedStatement` para prevenir inyección SQL, sin excepciones. La aplicación no ejecuta DDL: el esquema vive en `sql/`.
- **RNF-02b (Errores sin detalles internos):** el usuario nunca ve trazas ni mensajes técnicos. El detalle va a `coliseum.log` (`config/Registro`) y la interfaz muestra un mensaje de una excepción propia (`ColiseumException` y derivadas).
- **RNF-02c (Credenciales fuera del código):** el usuario, la contraseña, el host y el resto de los datos de conexión se leen de variables de entorno o de `.env` (`config/Configuracion`). No hay valores por defecto para usuario ni contraseña, y `.env` está en `.gitignore`.

### 2. Rendimiento y usabilidad
- **RNF-03 (Refresco del cronómetro):** el cronómetro debe actualizarse cada 50 ms con un `javax.swing.Timer`.
- **RNF-04 (Coherencia visual):** la interfaz debe usar una paleta centralizada (`Tema`) y estilos homogéneos (`BotonEstilo`), con respuesta visual al pasar el mouse (hover).

### 3. Mantenibilidad y arquitectura
- **RNF-05 (Liberación de recursos):** el acceso a datos debe usar `try-with-resources` para cerrar conexiones, consultas y `ResultSet`.
- **RNF-06 (Plataforma):** la aplicación debe ejecutarse en cualquier entorno Java 11 o superior con soporte para Swing.
- **RNF-07 (Base de datos):** la aplicación requiere un servidor MySQL 8 accesible según `DB_HOST`, `DB_PORT` y `DB_NAME`: el contenedor de `docker compose` (`DB_SSL_MODE=DISABLED`) o Aiven en la nube (`DB_SSL_MODE=REQUIRED`).

### 4. Notificaciones y segundo plano
- **RNF-08 (Esquema versionado):** el esquema se crea con `sql/01-crear-tablas.sql` (idempotente: `CREATE TABLE IF NOT EXISTS`) y los datos de prueba con `sql/02-datos-prueba.sql` (más de 20 registros, `INSERT IGNORE`). La aplicación no crea ni altera tablas, así que no necesita permisos de administrador.
- **RNF-09 (Revisión periódica):** un hilo de fondo (`coliseum-recordatorios`) debe revisar cada 30 segundos a quién corresponde avisar, sin bloquear la interfaz.
- **RNF-10 (Interfaz sin bloqueos):** la comprobación de los ajustes de Windows debe hacerse en un hilo aparte para no congelar la ventana.
- **RNF-11 (Notificación con identidad propia):** en Windows, la notificación debe mostrarse con el nombre «Coliseum» y el logo (mediante PowerShell). Si no es posible, se usa la notificación clásica de la bandeja de Java.
- **RNF-12 (Compatibilidad de notificaciones):** las notificaciones de escritorio requieren un sistema con bandeja del sistema; el estado de las notificaciones de Windows solo se consulta en Windows.
- **RNF-13 (Registro de eventos):** los avisos enviados, los reintentos y los diagnósticos quedan en `recordatorios.log`; los errores técnicos de acceso a datos, en `coliseum.log`.
- **RNF-14 (Tolerancia a fallos):** si no se puede mostrar un aviso, se reintenta en la siguiente pasada dentro de la ventana; si falla el registro de avisos en la base, se usa un registro en memoria.
- **RNF-15 (Un solo servicio):** no puede haber dos servicios de avisos activos a la vez (bloqueo de archivo y puerto local propio de cada usuario, `-Dcoliseum.puerto` para forzarlo).
- **RNF-16 (Compatibilidad gráfica):** la aceleración por GPU de Java2D queda desactivada por defecto para evitar colores mal renderizados; se puede reactivar con `-Dcoliseum.gpu=true`.

### 5. Despliegue y operación
- **RNF-17 (Arquitectura en capas):** presentación (Swing y `ConsolaMain`) → servicio → interfaces `dao/*DAO` → implementaciones `dao/jdbc/*DAOJdbc`, con el modelo en `modelo/`. Las capas se comunican mediante interfaces.
- **RNF-18 (Contenerización):** la app y la base deben correr en contenedores con `docker compose up`, con la imagen publicada en GitHub Container Registry y un usuario no root dentro del contenedor.
- **RNF-19 (Nube):** la aplicación debe funcionar contra una base MySQL en Aiven con SSL, con los mismos datos visibles desde dos PC distintas.
- **RNF-20 (Orquestación):** la app y la base deben poder desplegarse en Kubernetes (minikube o kind) con `Deployment`, `Service`, `ConfigMap`, `Secret`, `requests/limits` y probes.
- **RNF-21 (Costo cero):** todo el despliegue usa servicios gratuitos y no pide tarjeta de crédito.
- **RNF-22 (Reproducibilidad):** otra persona debe poder repetir el despliegue siguiendo solo `README.md` y `docs/guia-despliegue.md`.

## Trazabilidad con los requisitos mínimos de la consigna (R1 a R12)

| Id | Requisito de la consigna | Requisitos de este documento |
|---|---|---|
| R1 | Modelo de datos | RNF-08 |
| R2 | CRUD completo | RF-10, RF-29, RF-30, RF-08, RF-09 |
| R3 | Usuarios y roles | RF-01 a RF-05, RF-28, RNF-01 |
| R4 | Arquitectura en capas | RNF-17 |
| R5 | Orientación a objetos | RF-28 (jerarquía `Usuario` / `UsuarioComun` / `Administrador`), RNF-01 (interfaz `HashContrasena` con dos implementaciones) |
| R6 | Consultas | RF-31 |
| R7 | Transacción | RF-32 |
| R8 | Validaciones y errores | RF-04, RF-33, RNF-02b |
| R9 | Consultas seguras | RNF-02 |
| R10 | Configuración externa | RNF-02c |
| R11 | Datos de prueba | RNF-08 |
| R12 | Estado del servicio | RF-34 |
| Nivel 1 | Contenerizar | RNF-18 |
| Nivel 2 | Nube | RNF-19 |
| Nivel 3 | Kubernetes | RNF-20 |
