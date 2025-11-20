-- ===========================================
-- GTM - Datos Iniciales (Seed)
-- ===========================================

-- Insertar roles básicos
INSERT IGNORE INTO roles (id, name) VALUES
   (1, 'USER'),
   (2, 'ADMIN');

-- Usuario administrador por defecto
-- Hash BCrypt con costo 12
INSERT IGNORE INTO usuarios (id, email, password_hash, nombre_visible, enabled)
VALUES (1, 'admin@example.com', '$2b$12$D2kW9eCFA8pIO/6z3N1lyOy8d4wYe4oamRDNhlroMa72wptQ6rAtu', 'GTM_ADMIN', 1);

-- Asignar rol ADMIN al usuario administrador
INSERT IGNORE INTO usuarios_roles (usuario_id, role_id)
SELECT u.id, r.id
FROM usuarios u CROSS JOIN roles r
WHERE u.email = 'admin@example.com' AND r.name = 'ADMIN';