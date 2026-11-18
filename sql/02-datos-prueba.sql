-- Coliseum - datos de prueba (más de 20 registros). Solo para desarrollo y demostración.
-- Cuentas de prueba (las contraseñas están guardadas como hash, nunca en texto plano):
--   admin  -> rol ADMIN,   contraseña de demostración indicada en docs/guia-despliegue.md
--   ana, bruno, carla, diego -> rol USUARIO, misma contraseña de demostración
-- Ejecutar DESPUÉS de 01-crear-tablas.sql. INSERT IGNORE permite repetirlo sin errores.

INSERT IGNORE INTO usuarios (id, nombre_usuario, contrasena_hash, rol, notificaciones) VALUES
    (1, 'admin', 'pbkdf2$120000$Dqrt44SddVHTtCuPCACuVw==$jg94A75zocZFri+23/bWHNaGNJdR7m8HDhV9mUE+jtk=', 'ADMIN', NULL),
    (2, 'ana',   'pbkdf2$120000$PcyLbpc6zGZaMJetPbNWGQ==$i9zarcpmpxaE3NFmbDsehmeD1pFJPoXk6iMmo6SRyRA=', 'USUARIO', NULL),
    (3, 'bruno', 'pbkdf2$120000$PcyLbpc6zGZaMJetPbNWGQ==$i9zarcpmpxaE3NFmbDsehmeD1pFJPoXk6iMmo6SRyRA=', 'USUARIO', NULL),
    (4, 'carla', 'pbkdf2$120000$PcyLbpc6zGZaMJetPbNWGQ==$i9zarcpmpxaE3NFmbDsehmeD1pFJPoXk6iMmo6SRyRA=', 'USUARIO', NULL),
    (5, 'diego', 'pbkdf2$120000$PcyLbpc6zGZaMJetPbNWGQ==$i9zarcpmpxaE3NFmbDsehmeD1pFJPoXk6iMmo6SRyRA=', 'USUARIO', NULL);

INSERT IGNORE INTO rutina_semanal (usuario_id, dia_semana, resumen, ejercicios, hora) VALUES
    (2, 'MONDAY', 'Pecho y tríceps', '', '18:00:00'),
    (2, 'TUESDAY', 'Piernas', '', '18:00:00'),
    (2, 'WEDNESDAY', 'Descanso', '', NULL),
    (2, 'THURSDAY', 'Espalda y bíceps', '', '18:30:00'),
    (2, 'FRIDAY', 'Hombros y abdomen', '', '18:00:00'),
    (2, 'SATURDAY', 'Cardio suave', '', '10:00:00'),
    (2, 'SUNDAY', 'Descanso', '', NULL),
    (3, 'MONDAY', 'Piernas', '', '19:00:00'),
    (3, 'TUESDAY', 'Descanso', '', NULL),
    (3, 'WEDNESDAY', 'Pecho', '', '19:00:00'),
    (3, 'THURSDAY', 'Descanso', '', NULL),
    (3, 'FRIDAY', 'Espalda', '', '19:00:00'),
    (3, 'SATURDAY', 'Full body', '', '11:00:00'),
    (3, 'SUNDAY', 'Descanso', '', NULL),
    (4, 'MONDAY', 'Cardio', '', '07:00:00'),
    (4, 'TUESDAY', 'Fuerza superior', '', '07:00:00'),
    (4, 'WEDNESDAY', 'Descanso', '', NULL),
    (4, 'THURSDAY', 'Fuerza inferior', '', '07:00:00'),
    (4, 'FRIDAY', 'Cardio', '', '07:00:00'),
    (4, 'SATURDAY', 'Descanso', '', NULL),
    (4, 'SUNDAY', 'Descanso', '', NULL);

INSERT IGNORE INTO rutinas_dia (usuario_id, fecha, descripcion) VALUES
    (2, '2026-11-16', 'Press banca 4x8 y fondos'),
    (2, '2026-11-17', 'Sentadilla 5x5 y prensa'),
    (2, '2026-11-19', 'Dominadas y remo con barra'),
    (2, '2026-11-20', 'Press militar y abdominales'),
    (2, '2026-11-21', 'Trote 30 minutos'),
    (2, '2026-12-02', 'Test de fuerza: sentadilla 1RM'),
    (3, '2026-11-16', 'Sentadilla frontal y zancadas'),
    (3, '2026-11-18', 'Press inclinado con mancuernas'),
    (3, '2026-11-20', 'Peso muerto 5x3'),
    (3, '2026-11-22', 'Full body con kettlebell'),
    (3, '2026-12-01', 'Descarga: movilidad y estiramientos'),
    (4, '2026-11-16', 'Intervalos 8x400 m'),
    (4, '2026-11-17', 'Press de pecho y remo con mancuernas'),
    (4, '2026-11-19', 'Sentadilla búlgara y puente de glúteos'),
    (4, '2026-11-20', 'Cinta 40 minutos'),
    (4, '2026-12-03', 'Medición de marcas personales'),
    (5, '2026-11-18', 'Primera clase de adaptación'),
    (5, '2026-11-25', 'Circuito de cuerpo completo'),
    (5, '2026-12-05', 'Natación 1 km'),
    (1, '2026-11-17', 'Revisión de rutinas de los usuarios nuevos');

INSERT IGNORE INTO avisos_entreno (usuario_id, fecha, canal) VALUES
    (2, '2026-11-16', 'toast'),
    (3, '2026-11-16', 'toast'),
    (4, '2026-11-16', 'toast');
