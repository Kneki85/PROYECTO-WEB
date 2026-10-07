-- =====================================================================
-- Migración de la versión 1 a la versión 2 SIN perder datos
-- (agrega usuarios/login y "quién hizo el cambio" en el historial)
--
-- Úsalo SOLO si ya tenías la base "incidencias_db" con datos y quieres conservarlos.
-- Ejecútalo UNA sola vez (si lo repites dará error porque los cambios ya existen).
-- Si empiezas de cero, usa incidencias_db.sql.
-- =====================================================================

USE incidencias_db;

CREATE TABLE usuario (
    id_usuario          INT AUTO_INCREMENT PRIMARY KEY,
    username            VARCHAR(50)  NOT NULL,
    nombre_completo     VARCHAR(120) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    rol                 ENUM('ADMIN','CONSULTA') NOT NULL DEFAULT 'CONSULTA',
    activo              TINYINT(1)   NOT NULL DEFAULT 1,
    debe_cambiar_clave  TINYINT(1)   NOT NULL DEFAULT 0,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE historial_incidencia
    ADD COLUMN id_usuario INT NULL,
    ADD CONSTRAINT fk_historial_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE SET NULL;

ALTER TABLE incidencia
    ADD INDEX idx_incidencia_estado (estado),
    ADD INDEX idx_incidencia_fecha (fecha_registro);

-- Usuario administrador inicial (contraseña: Admin2026*, se pedirá cambiarla al primer ingreso)
INSERT INTO usuario (username, nombre_completo, password_hash, rol, activo, debe_cambiar_clave) VALUES
    ('admin', 'Administrador del sistema',
     'pbkdf2_sha256$600000$BE2EWDmRLVRwp+74wlKxcg==$wfW0OHTErrmgX8veiseVwD2dDae6SPMhxLF4HAd0rOM=',
     'ADMIN', 1, 1);
