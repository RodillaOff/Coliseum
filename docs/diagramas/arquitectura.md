# Arquitectura de despliegue

Proyecto de **escritorio**: no usa Render. La ventana corre en la PC y se conecta a la base; en Docker y Kubernetes corre el modo servicio (sin ventana).

```mermaid
flowchart LR
    subgraph PC["PC del usuario"]
        GUI["Coliseum (ventana Swing)"]
    end
    subgraph Local["Docker / docker-compose"]
        APP["Contenedor app<br/>modo servicio :8080/health"]
        DBL[("PostgreSQL 16 local")]
    end
    subgraph K8S["Kubernetes local (minikube)"]
        PODA["Pod coliseum-app<br/>Deployment + Service"]
        PODD["Pod coliseum-db<br/>Deployment + Service + PVC"]
        CM["ConfigMap"]
        SEC["Secret"]
    end
    AIVEN[("Aiven<br/>PostgreSQL en la nube (SSL)")]
    GH["Repositorio GitHub<br/>código, Dockerfile, workflows"]
    GHCR["ghcr.io<br/>imagen coliseum"]

    GUI -- "JDBC + SSL" --> AIVEN
    APP --> DBL
    PODA --> PODD
    CM -.-> PODA
    SEC -.-> PODA
    SEC -.-> PODD
    GH -- "GitHub Actions publica" --> GHCR
    GHCR -- "usa la imagen" --> APP
    GHCR -- "usa la imagen" --> PODA
```

# Capas de la aplicación

```mermaid
flowchart TB
    P["Presentación<br/>Swing (Coliseum, paneles) · ConsolaMain"]
    S["Servicio<br/>UsuarioServicio · RutinaServicio · ReporteServicio · SaludServicio · Validador"]
    D["Interfaces DAO<br/>UsuarioDAO · RutinaSemanalDAO · RutinaDiaDAO · AvisoDAO · ReporteDAO"]
    J["Implementación JDBC<br/>dao/jdbc (PreparedStatement)"]
    C["config<br/>Configuracion (variables de entorno) · Conexion"]
    DB[("PostgreSQL")]
    P --> S --> D --> J --> DB
    S --> C
    J -.usa la conexión.-> C
```
