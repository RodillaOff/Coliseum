# Diagramas UML - Coliseum

Los diagramas muestran la versión actual: arquitectura en capas (R4), jerarquía de herencia e interfaz con dos implementaciones (R5), roles (R3) y excepciones propias (R8).

## 1. Capas y paquetes

```mermaid
flowchart TB
    subgraph PRES["Presentación"]
        SW["Swing: Coliseum, LoginDialog, CalendarioPanel,<br/>ReportesPanel, AdminPanel, CuentaPanel, EstadoPanel"]
        CON["consola.ConsolaMain<br/>--consola · --servicio · --health"]
    end
    subgraph SERV["servicio"]
        US["UsuarioServicio"]
        RS["RutinaServicio"]
        RP["ReporteServicio"]
        SA["SaludServicio"]
        VA["Validador"]
    end
    subgraph DAOI["dao (interfaces)"]
        DI["UsuarioDAO · RutinaSemanalDAO · RutinaDiaDAO<br/>AvisoDAO · ReporteDAO"]
    end
    subgraph DAOJ["dao.jdbc (PreparedStatement)"]
        DJ["UsuarioDAOJdbc · RutinaSemanalDAOJdbc · RutinaDiaDAOJdbc<br/>AvisoDAOJdbc · ReporteDAOJdbc"]
    end
    CFG["config<br/>Configuracion · Conexion · Registro"]
    MOD["modelo<br/>Usuario · Rol · RutinaDia · RutinaSemanal · FilaReporte · EstadoServicio"]
    SEG["seguridad<br/>HashContrasena · Pbkdf2Hash · Sha256Hash · Hashes"]
    EXC["excepciones<br/>ColiseumException y derivadas"]
    DB[("PostgreSQL<br/>local en Docker o Aiven")]

    SW --> SERV
    CON --> SERV
    SERV --> DI
    DI -.implementan.- DJ
    DJ --> DB
    SERV --> CFG
    SERV --> SEG
    SERV --> VA
    SERV --> EXC
    SERV --> MOD
    DJ --> MOD
    CFG -->|"variables de entorno / .env"| DB
```

## 2. Modelo de dominio y seguridad (R3, R5)

```mermaid
classDiagram
    direction TB

    class Rol {
        <<enumeration>>
        ADMIN
        USUARIO
        +getEtiqueta() String
        +desde(texto)$ Rol
    }

    class Usuario {
        <<abstract>>
        -id int
        -nombre String
        -hashContrasena String
        -notificaciones Boolean
        +getRol()* Rol
        +puedeAdministrar()* boolean
        +crear(rol, id, nombre, hash, notificaciones)$ Usuario
    }
    class UsuarioComun {
        +getRol() Rol
        +puedeAdministrar() boolean
    }
    class Administrador {
        +getRol() Rol
        +puedeAdministrar() boolean
    }

    class HashContrasena {
        <<interface>>
        +hashear(plano) String
        +verifica(plano, guardado) boolean
        +reconoce(guardado) boolean
    }
    class Pbkdf2Hash {
        PBKDF2-HMAC-SHA256, sal, 120000 iteraciones
    }
    class Sha256Hash {
        heredado, solo para migrar cuentas viejas
    }
    class Hashes {
        +actual()$ HashContrasena
        +verificar(plano, guardado)$ boolean
        +necesitaActualizar(guardado)$ boolean
    }

    class RutinaSemanal {
        -dia DayOfWeek
        -resumen String
        -ejercicios String
        -hora LocalTime
    }
    class RutinaDia {
        -id int
        -fecha LocalDate
        -descripcion String
    }

    Usuario <|-- UsuarioComun
    Usuario <|-- Administrador
    Usuario ..> Rol
    HashContrasena <|.. Pbkdf2Hash
    HashContrasena <|.. Sha256Hash
    Hashes ..> HashContrasena : uso polimórfico
```

## 3. Servicios, DAO y excepciones (R4, R7, R8, R9)

```mermaid
classDiagram
    direction LR

    class UsuarioServicio {
        +registrar(nombre, contrasena) boolean
        +autenticar(nombre, contrasena) Optional~Usuario~
        +cambiarContrasena(id, actual, nueva) void
        +renombrar(id, contrasena, nuevo) void
        +eliminarCuenta(id, contrasena) void
        +listar(actor) List~Usuario~
        +cambiarRol(actor, id, rol) void
        +restablecerContrasena(actor, id, nueva) void
        +eliminarUsuario(actor, id) void
        -exigirAdmin(con, actor) void
    }
    class RutinaServicio {
        +guardarSemana(usuarioId, resumenes, horas) void
        +cargarSemana(usuarioId) List~RutinaSemanal~
        +agregarNota(usuarioId, fecha, texto) void
        +listarNotas(usuarioId, fecha) List~String~
        +editarNota(usuarioId, fecha, actual, nuevo) void
        +eliminarNota(usuarioId, fecha, texto) void
    }
    class ReporteServicio {
        +buscar(actor, texto, desde, hasta) List~RutinaDia~
        +resumenPorUsuario(actor) List~FilaReporte~
        +notasPorMes(actor) Map
        +promedioNotasPorUsuario() double
    }
    class SaludServicio {
        +verificar() EstadoServicio
    }
    class Validador {
        +nombreUsuario(n)$ String
        +contrasena(c)$ void
        +texto(campo, v, max)$ String
        +hora(t)$ String
        +fecha(f)$ LocalDate
        +rango(desde, hasta)$ void
    }

    class UsuarioDAO {
        <<interface>>
    }
    class RutinaSemanalDAO {
        <<interface>>
    }
    class RutinaDiaDAO {
        <<interface>>
    }
    class AvisoDAO {
        <<interface>>
    }
    class ReporteDAO {
        <<interface>>
    }
    class UsuarioDAOJdbc
    class RutinaSemanalDAOJdbc
    class RutinaDiaDAOJdbc
    class AvisoDAOJdbc
    class ReporteDAOJdbc

    class Conexion {
        +obtener()$ Connection
        +descripcion()$ String
    }
    class Configuracion {
        +obtener(clave)$ String
    }

    class ColiseumException
    class ValidacionException
    class AutorizacionException
    class AccesoDatosException

    UsuarioServicio ..> UsuarioDAO
    RutinaServicio ..> RutinaSemanalDAO
    RutinaServicio ..> RutinaDiaDAO
    RutinaServicio ..> AvisoDAO
    ReporteServicio ..> ReporteDAO
    UsuarioServicio ..> Validador
    RutinaServicio ..> Validador

    UsuarioDAO <|.. UsuarioDAOJdbc
    RutinaSemanalDAO <|.. RutinaSemanalDAOJdbc
    RutinaDiaDAO <|.. RutinaDiaDAOJdbc
    AvisoDAO <|.. AvisoDAOJdbc
    ReporteDAO <|.. ReporteDAOJdbc

    SaludServicio ..> Conexion
    RutinaServicio ..> Conexion : transacción (commit / rollback)
    Conexion ..> Configuracion

    ColiseumException <|-- ValidacionException
    ColiseumException <|-- AutorizacionException
    ColiseumException <|-- AccesoDatosException
```

## 4. Casos de uso

```mermaid
flowchart LR
    U([Usuario])
    A([Administrador])
    BD[(Base de datos)]
    WIN([Windows])

    subgraph SIS[Sistema Coliseum]
        direction TB
        UC1([Registrarse])
        UC2([Iniciar sesión])
        UC3([Ver frase motivacional])
        UC4([Ver calendario])
        UC5([Configurar rutina semanal])
        UC6([Agregar, editar o quitar notas de un día])
        UC7([Ver reloj])
        UC8([Usar cronómetro])
        UC9([Activar o desactivar notificaciones])
        UC10([Probar notificación])
        UC11([Recibir aviso de entreno])
        UC12([Dejar la app en segundo plano])
        UC13([Ver reportes y buscar con filtros])
        UC14([Cambiar contraseña, nombre o eliminar mi cuenta])
        UC15([Verificar el estado de la conexión])
        UC16([Administrar usuarios: rol, contraseña, baja])
        UC17([Ver reportes de todos los usuarios])
    end

    U --- UC1
    U --- UC2
    U --- UC3
    U --- UC4
    U --- UC5
    U --- UC6
    U --- UC7
    U --- UC8
    U --- UC9
    U --- UC10
    U --- UC11
    U --- UC12
    U --- UC13
    U --- UC14
    U --- UC15
    A -. "hereda todo lo de Usuario" .- U
    A --- UC16
    A --- UC17

    UC1 --- BD
    UC2 --- BD
    UC4 --- BD
    UC5 --- BD
    UC6 --- BD
    UC9 --- BD
    UC11 --- BD
    UC13 --- BD
    UC14 --- BD
    UC15 --- BD
    UC16 --- BD
    UC17 --- BD

    UC10 --- WIN
    UC11 --- WIN
```

> Las clases de presentación y de recordatorios que viven en `Coliseum.java` (`Coliseum`, `LoginDialog`, `CalendarioPanel`, `Recordatorios`, `Bandeja`, `ToastWindows`, etc.) no se repiten aquí: llaman a la capa de servicio a través de la fachada `ConexionBD`, que ya no contiene SQL.
