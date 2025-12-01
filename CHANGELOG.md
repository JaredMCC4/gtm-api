# Changelog

All notable changes to this project will be documented in this file. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.1] - 2025-12-01
### Added
- Protección anti-bots en autenticación usando Cloudflare Turnstile: validación del token recibido del frontend para el flujo de registro/login.

### Changed
- Bump de versión del artefacto a `1.1.1` y actualización del README con la nueva versión.

## [1.1.0] - 2025-11-28
### Added
- Login social vía OAuth2 con Google y GitHub: intercambio de `code`/`access_token`, obtención de perfil (incluyendo email primario en GitHub) y emisión de JWT + refresh propios.
- Endpoint público `POST /api/v1/auth/oauth/login` para completar el flujo social desde el frontend.
- Propiedades de configuración para proveedores (`oauth.google.*`, `oauth.github.*`) en el archivo de ejemplo.

### Changed
- Bump de versión del artefacto a `1.1.0` y actualización del README con la nueva versión.

## [1.0.0] - 2025-11-27
### Added
- API pública para autenticación (registro, login, refresh, logout, validación) con JWT HS256 y refresh tokens persistidos.
- CRUD completo de tareas, subtareas, etiquetas, adjuntos y perfil de usuario con validaciones, paginación y filtrado.
- Migraciones Flyway para crear esquema inicial y sembrar datos base, más datos locales de desarrollo.
- Documentación OpenAPI/Swagger (`/swagger`, `/v3/api-docs`) y JavaDocs generables con Maven.
- Dockerfile multi-stage y `docker-compose.yml` con servicio MySQL y healthchecks listos para desarrollo.
- Suite de pruebas unitarias e integradas con cobertura JaCoCo (70% líneas / 60% ramas mínimas).
- Observabilidad inicial con Spring Boot Actuator (`/actuator/health` público, resto protegido).

### Changed
- Versionado del artefacto a `1.0.0` con metadata de licencia, desarrollador y SCM completa.
- Documentación alineada al puerto por defecto `2828` y ejemplos de configuración actualizados.
- Pipeline CI/CD inicial preparado para linting del Dockerfile, pruebas Maven e imagen de contenedor.

### Security & Privacy
- Reglas de autorización claras por rol (USER/ADMIN) y CORS configurable por entorno.
- Exclusión explícita de `.env` y secretos del contexto de build y de las imágenes publicadas.
