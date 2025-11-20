-- ===========================================
-- GTM - Schema Inicial (MySQL 8)
-- ===========================================

-- Tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    INDEX idx_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   email VARCHAR(190) NOT NULL UNIQUE,
   password_hash VARCHAR(100) NOT NULL,
   nombre_visible VARCHAR(120),
   zona_horaria VARCHAR(64) DEFAULT 'America/Costa_Rica',
   enabled TINYINT(1) NOT NULL DEFAULT 1,
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   INDEX idx_usuarios_email (email),
   INDEX idx_usuarios_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla intermedia usuarios_roles
CREATE TABLE IF NOT EXISTS usuarios_roles (
    usuario_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de tareas
CREATE TABLE IF NOT EXISTS tareas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    titulo VARCHAR(120) NOT NULL,
    descripcion TEXT,
    prioridad ENUM('BAJA','MEDIA','ALTA') NOT NULL DEFAULT 'MEDIA',
    estado ENUM('PENDIENTE','COMPLETADA','CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_vencimiento DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tarea_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_titulo_len CHECK (CHAR_LENGTH(titulo) BETWEEN 3 AND 120),
    INDEX idx_tareas_user_estado (usuario_id, estado),
    INDEX idx_tareas_user_prioridad (usuario_id, prioridad),
    INDEX idx_tareas_vencimiento (fecha_vencimiento),
    INDEX idx_tareas_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de subtareas
CREATE TABLE IF NOT EXISTS subtareas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tarea_id BIGINT NOT NULL,
    titulo VARCHAR(120) NOT NULL,
    completada TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_sub_tarea FOREIGN KEY (tarea_id) REFERENCES tareas(id) ON DELETE CASCADE,
    INDEX idx_sub_tarea (tarea_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de etiquetas
CREATE TABLE IF NOT EXISTS etiquetas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    color_hex CHAR(7) NOT NULL,
    CONSTRAINT fk_etq_user FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_color_hex CHECK (color_hex REGEXP '^#[0-9A-Fa-f]{6}$'),
    UNIQUE KEY uk_etiqueta_usuario_nombre (usuario_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla intermedia tarea_etiquetas
CREATE TABLE IF NOT EXISTS tarea_etiquetas (
   tarea_id BIGINT NOT NULL,
   etiqueta_id BIGINT NOT NULL,
   PRIMARY KEY (tarea_id, etiqueta_id),
   CONSTRAINT fk_te_tarea FOREIGN KEY (tarea_id) REFERENCES tareas(id) ON DELETE CASCADE,
   CONSTRAINT fk_te_etiqueta FOREIGN KEY (etiqueta_id) REFERENCES etiquetas(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de adjuntos
CREATE TABLE IF NOT EXISTS adjuntos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tarea_id BIGINT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    path VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_adj_tarea FOREIGN KEY (tarea_id) REFERENCES tareas(id) ON DELETE CASCADE,
    CONSTRAINT chk_mime CHECK (mime_type REGEXP '^[A-Za-z0-9.+\\-]+/[A-Za-z0-9.+\\-]+$'),
    INDEX idx_adj_tarea (tarea_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de refresh tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   usuario_id BIGINT NOT NULL,
   token VARCHAR(500) NOT NULL UNIQUE,
   expires_at DATETIME NOT NULL,
   revoked TINYINT(1) NOT NULL DEFAULT 0,
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT fk_rt_user FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
   INDEX idx_rt_user (usuario_id),
   INDEX idx_rt_exp (expires_at),
   INDEX idx_rt_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;