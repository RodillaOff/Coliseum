-- Coliseum - creación de tablas (MySQL 8).
-- Es idempotente: se puede ejecutar más de una vez. NO borra datos ni tablas.
-- No crea la base ni usa USE: se ejecuta contra la base ya elegida
--   (Docker: MYSQL_DATABASE; Aiven: la base que creaste en la consola, por ejemplo defaultdb).

CREATE TABLE IF NOT EXISTS usuarios (
    id              INT          NOT NULL AUTO_INCREMENT,
    nombre_usuario  VARCHAR(100) NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL,                 -- PBKDF2 con sal (o SHA-256 heredado)
    rol             VARCHAR(10)  NOT NULL DEFAULT 'USUARIO',
    notificaciones  TINYINT(1)   NULL,                     -- NULL = todavía no eligió
    fecha_registro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_usuarios_nombre (nombre_usuario),
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('ADMIN', 'USUARIO'))
) ENGINE=InnoDB;

-- Relación 1:N  usuarios -> rutina_semanal
CREATE TABLE IF NOT EXISTS rutina_semanal (
    usuario_id  INT          NOT NULL,
    dia_semana  VARCHAR(10)  NOT NULL,                     -- MONDAY ... SUNDAY
    resumen     VARCHAR(255) NOT NULL DEFAULT '',
    ejercicios  TEXT         NOT NULL,
    hora        TIME         NULL,
    PRIMARY KEY (usuario_id, dia_semana),
    CONSTRAINT fk_rutina_semanal_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Relación 1:N  usuarios -> rutinas_dia
CREATE TABLE IF NOT EXISTS rutinas_dia (
    id          INT          NOT NULL AUTO_INCREMENT,
    usuario_id  INT          NOT NULL,
    fecha       DATE         NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_rutinas_dia_usuario_fecha (usuario_id, fecha),
    CONSTRAINT fk_rutinas_dia_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Relación 1:N  usuarios -> avisos_entreno (qué avisos ya se enviaron)
CREATE TABLE IF NOT EXISTS avisos_entreno (
    usuario_id INT         NOT NULL,
    fecha      DATE        NOT NULL,
    canal      VARCHAR(10) NOT NULL,
    PRIMARY KEY (usuario_id, fecha, canal),
    CONSTRAINT fk_avisos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB;
