-- =====================================================================
-- GestPersonal - Sistema de Control de Incidencias para la Gestión de Personal
-- Script de creación de base de datos para MySQL/MariaDB (XAMPP)   [versión 2]
--
-- Cómo usarlo:
--   1. Abre phpMyAdmin (http://localhost/phpmyadmin) con XAMPP iniciado
--      (Apache y MySQL en verde).
--   2. Ve a la pestaña "SQL" y pega/ejecuta todo este archivo.
--      (O: Importar -> selecciona este archivo -> Continuar).
--
-- ATENCIÓN: este script BORRA y vuelve a crear todas las tablas.
-- Si ya tienes datos que quieres conservar, usa migracion_v2.sql en su lugar.
--
-- Usuario inicial:  admin   /   Admin2026*
-- Al entrar por primera vez el sistema te obligará a cambiar esa contraseña.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS incidencias_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE incidencias_db;

-- Orden de borrado: primero las tablas "hijas" (con llaves foráneas)
DROP TABLE IF EXISTS historial_incidencia;
DROP TABLE IF EXISTS incidencia;
DROP TABLE IF EXISTS tipo_incidencia;
DROP TABLE IF EXISTS empleado;
DROP TABLE IF EXISTS usuario;

-- ---------------------------------------------------------------------
-- Tabla: usuario (quienes pueden iniciar sesión)
-- La contraseña NUNCA se guarda: se guarda un hash PBKDF2 con sal (ver seguridad/Contrasenas.java)
-- ---------------------------------------------------------------------
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

-- ---------------------------------------------------------------------
-- Tabla: empleado
-- ---------------------------------------------------------------------
CREATE TABLE empleado (
    id_empleado     INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(80)  NOT NULL,
    apellido        VARCHAR(80)  NOT NULL,
    cargo           VARCHAR(80),
    area            VARCHAR(80),
    fecha_ingreso   DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Tabla: tipo_incidencia (catálogo, administrado directo en BD)
-- ---------------------------------------------------------------------
CREATE TABLE tipo_incidencia (
    id_tipo         INT AUTO_INCREMENT PRIMARY KEY,
    nombre_tipo     VARCHAR(80) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Tabla: incidencia
-- ---------------------------------------------------------------------
CREATE TABLE incidencia (
    id_incidencia   INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado     INT NOT NULL,
    id_tipo         INT NOT NULL,
    descripcion     TEXT,
    fecha_registro  DATE NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'Abierta',
    CONSTRAINT fk_incidencia_empleado
        FOREIGN KEY (id_empleado) REFERENCES empleado(id_empleado)
        ON DELETE CASCADE,
    CONSTRAINT fk_incidencia_tipo
        FOREIGN KEY (id_tipo) REFERENCES tipo_incidencia(id_tipo)
        ON DELETE RESTRICT,
    INDEX idx_incidencia_estado (estado),
    INDEX idx_incidencia_fecha (fecha_registro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Tabla: historial_incidencia (registro de cambios de estado y de quién los hizo)
-- estado_anterior es NULL en el registro inicial de la incidencia.
-- ---------------------------------------------------------------------
CREATE TABLE historial_incidencia (
    id_historial    INT AUTO_INCREMENT PRIMARY KEY,
    id_incidencia   INT NOT NULL,
    estado_anterior VARCHAR(20),
    estado_nuevo    VARCHAR(20),
    fecha_cambio    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_usuario      INT NULL,
    CONSTRAINT fk_historial_incidencia
        FOREIGN KEY (id_incidencia) REFERENCES incidencia(id_incidencia)
        ON DELETE CASCADE,
    CONSTRAINT fk_historial_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Datos iniciales
-- ---------------------------------------------------------------------

-- Usuario administrador inicial (contraseña: Admin2026*, se pedirá cambiarla al primer ingreso)
INSERT INTO usuario (username, nombre_completo, password_hash, rol, activo, debe_cambiar_clave) VALUES
    ('admin', 'Administrador del sistema',
     'pbkdf2_sha256$600000$BE2EWDmRLVRwp+74wlKxcg==$wfW0OHTErrmgX8veiseVwD2dDae6SPMhxLF4HAd0rOM=',
     'ADMIN', 1, 1);

INSERT INTO tipo_incidencia (nombre_tipo) VALUES
    ('Tardanza'),
    ('Falta injustificada'),
    ('Permiso'),
    ('Licencia médica'),
    ('Otro');

-- Datos de ejemplo (para poder probar la app de inmediato; puedes borrarlos desde la aplicación)
INSERT INTO empleado (nombre, apellido, cargo, area, fecha_ingreso) VALUES
    ('Juan', 'Pérez', 'Analista', 'Sistemas', '2023-03-01'),
    ('María', 'Gómez', 'Supervisora', 'Recursos Humanos', '2021-07-15'),
    ('Carlos', 'Rojas', 'Técnico', 'Soporte', '2022-11-20'),
    ('Yair', 'Vega', 'Asistente', 'Administración', '2024-02-05'),
    ('Jhon', 'Sánchez', 'Desarrollador', 'Sistemas', '2023-09-18');

INSERT INTO incidencia (id_empleado, id_tipo, descripcion, fecha_registro, estado) VALUES
    (1, 1, 'Llegó 20 minutos tarde por tráfico.', CURDATE(), 'Abierta'),
    (2, 3, 'Solicitó permiso por trámite personal.', CURDATE(), 'Cerrada'),
    (3, 2, 'No se presentó a laborar y no avisó a su jefatura.', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'En proceso'),
    (1, 1, 'Ingresó 10 minutos después de la hora establecida.', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'Cerrada'),
    (4, 4, 'Presentó descanso médico por tres días.', DATE_SUB(CURDATE(), INTERVAL 7 DAY), 'Cerrada'),
    (5, 1, 'Llegó tarde a la reunión de coordinación.', DATE_SUB(CURDATE(), INTERVAL 9 DAY), 'Abierta'),
    (1, 3, 'Permiso por cita en una entidad pública.', DATE_SUB(CURDATE(), INTERVAL 12 DAY), 'Cerrada');

-- Historial de ejemplo: todas nacen "Abierta" y luego siguen su camino
INSERT INTO historial_incidencia (id_incidencia, estado_anterior, estado_nuevo, id_usuario)
    SELECT id_incidencia, NULL, 'Abierta', 1 FROM incidencia;
INSERT INTO historial_incidencia (id_incidencia, estado_anterior, estado_nuevo, id_usuario)
    SELECT id_incidencia, 'Abierta', 'En proceso', 1 FROM incidencia WHERE estado IN ('En proceso', 'Cerrada');
INSERT INTO historial_incidencia (id_incidencia, estado_anterior, estado_nuevo, id_usuario)
    SELECT id_incidencia, 'En proceso', 'Cerrada', 1 FROM incidencia WHERE estado = 'Cerrada';
