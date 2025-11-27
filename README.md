# Gestor de Tareas Moderno API (GTM-API)
[![Versión 1.0.0](https://img.shields.io/badge/Versi%C3%B3n-1.0.0-1E3A8A?style=for-the-badge&logo=semver&logoColor=ffffff)](CHANGELOG.md)
[![Java 21](https://img.shields.io/badge/Java-21-F89820?style=for-the-badge&logo=openjdk&logoColor=ffffff)](#)
[![Spring Boot 3.5.7](https://img.shields.io/badge/Spring%20Boot-3.5.7-00B050?style=for-the-badge&logo=springboot&logoColor=ffffff)](#)
[![Maven 3.9.11](https://img.shields.io/badge/Maven-3.9.11-B40124?style=for-the-badge&logo=apachemaven&logoColor=ffffff)](#)
[![MySQL 8](https://img.shields.io/badge/MySQL-8.0-1572B6?style=for-the-badge&logo=mysql&logoColor=ffffff)](#)
[![Flyway 11](https://img.shields.io/badge/Flyway-11.x-E4002B?style=for-the-badge&logo=flyway&logoColor=ffffff)](#)
[![JaCoCo](https://img.shields.io/badge/Cobertura-JaCoCo%2070%25%20L%20%2F%2060%25%20B-5E60CE?style=for-the-badge&logo=checkmarx&logoColor=ffffff)](#)
[![Licencia MIT](https://img.shields.io/badge/Licencia-MIT-111827?style=for-the-badge)](LICENSE)

## Descripción
API REST construida con Spring Boot que funciona como backend del Gestor de Tareas Moderno. Expone autenticación basada en JWT, gestión integral de tareas, subtareas, etiquetas, adjuntos y perfil de usuario sobre MySQL con migraciones Flyway, documentación OpenAPI y observabilidad vía Actuator.

## Índice
- [Features](#features)
- [Stack tecnológico y arquitectura](#stack-tecnológico-y-arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Prerrequisitos](#prerrequisitos)
- [Instalación y configuración](#instalación-y-configuración)
- [Archivos de configuración de ejemplo](#archivos-de-configuración-de-ejemplo)
- [Seguridad y autenticación](#seguridad-y-autenticación)
- [Ejecución de la aplicación](#ejecución-de-la-aplicación)
- [Ejecución de pruebas](#ejecución-de-pruebas)
- [Documentación de la API y documentos funcionales](#documentación-de-la-api-y-documentos-funcionales)
- [Migraciones de base de datos](#migraciones-de-base-de-datos)
- [Changelog](#changelog)
- [Licencia](#licencia)
- [Autor](#autor)

## Features
- Registro, inicio de sesión, refresco, logout y validación de tokens JWT mediante `AuthController`.
- CRUD de tareas (`/api/v1/tareas`) con paginación, ordenamiento, búsqueda por texto, filtros por estado/prioridad/título y estadísticas agregadas.
- Gestión de subtareas (`/api/v1/subtareas`) asociadas a cada tarea.
- Administración de etiquetas (`/api/v1/etiquetas`) con validaciones de unicidad y color hexadecimal.
- Manejo de adjuntos (subir, listar, descargar y eliminar) válido por usuario con validaciones de tipo/tamaño y almacenamiento configurable.
- Endpoints de perfil y cambio de contraseña (`/api/v1/usuarios`) respaldados por BCrypt.
- Respuestas normalizadas (`ApiResponse`/`PageResponse`), manejo global de errores, internacionalización básica (`messages*.properties`) y límites de carga (10 MB).
- Observabilidad con Spring Boot Actuator (`/actuator/health` público y métricas protegidas).

## Stack tecnológico y arquitectura
- **Backend:** Java 21, Spring Boot 3.5.7 (Web, Validation, Actuator, Security, OAuth2 Resource Server, Multipart).
- **Persistencia:** Spring Data JPA + Hibernate sobre MySQL 8 (`mysql-connector-j`), HikariCP.
- **Migraciones:** Flyway Core + Flyway MySQL (scripts en `src/main/resources/db/migration` y `db/migration/local` para datos de desarrollo).
- **Seguridad:** Spring Security 6, `NimbusJwtDecoder` + `jjwt` para tokens HMAC-SHA256 con BCrypt (coste 12) y refresh tokens persistidos.
- **Documentación:** `springdoc-openapi-starter-webmvc-ui` 2.8.14 mediante `OpenApiConfig`.
- **Testing:** JUnit 5, Spring Boot Test, Spring Security Test y Mockito. Los `@DataJpaTest` usan la conexión MySQL configurada (`@AutoConfigureTestDatabase(replace = NONE)`); no se emplea H2 ni Testcontainers aunque la dependencia exista.
- **Cobertura:** JaCoCo 0.8.11 con umbrales mínimos (70 % líneas, 60 % ramas) y reportes HTML/XML.
- **Utilidades:** Lombok, script `run-tests.sh`, generador de contraseñas (`src/scripts/password_encrypt_gen.py`).

**Capas y paquetes (bajo `io.github.jaredmcc4.gtm`):**
- `controller`: endpoints REST (Auth, Tarea, Usuario, etc.) documentados con OpenAPI.
- `services`: lógica de negocio por agregado (`TareaServiceImpl`, `AuthServiceImpl`, etc.).
- `repository`: repositorios JPA y consultas personalizadas.
- `domain`: entidades JPA (Usuario, Rol, Tarea, Subtarea, Etiqueta, Adjunto, RefreshToken) y enums.
- `dto` / `mapper`: requests-responses y transformaciones entidad/DTO.
- `config`: seguridad, CORS, OpenAPI, carga de archivos e i18n.
- `exception`, `util`, `validator`: excepciones, utilidades JWT/paginación y validaciones de archivos.

## Estructura del proyecto
```
gtm-api
|-- pom.xml
|-- run-tests.sh
|-- .env.example
|-- .env.test.example
|-- .env (local, no versionado)
|-- .env.test (local, no versionado)
|-- README.md
|-- HELP.md
|-- src
|   |-- main
|   |   |-- java
|   |   |   `-- io/github/jaredmcc4/gtm
|   |   |       |-- config
|   |   |       |-- controller
|   |   |       |-- domain
|   |   |       |-- dto
|   |   |       |-- exception
|   |   |       |-- mapper
|   |   |       |-- repository
|   |   |       |-- services
|   |   |       |-- util
|   |   |       `-- validator
|   |   `-- resources
|   |       |-- application.properties
|   |       |-- db
|   |       |   `-- migration
|   |       |       |-- V1__create_initial_schema.sql
|   |       |       |-- V2__seed_initial_data.sql
|   |       |       `-- local
|   |       |           `-- V2001__seed_datos_locales.sql
|   |       `-- messages*.properties
|   `-- test
|       |-- java
|       |   `-- io/github/jaredmcc4/gtm
|       |       |-- builders
|       |       |-- config
|       |       |-- controller
|       |       |-- domain
|       |       |-- i18n
|       |       |-- mapper
|       |       |-- migration
|       |       |-- repository
|       |       |-- security
|       |       |-- services
|       |       |-- util
|       |       `-- validator
|       `-- resources
|           `-- application.properties
`-- src/scripts
    `-- password_encrypt_gen.py
```

## Prerrequisitos
- JDK 21 (OpenJDK recomendado).
- Maven 3.9.11.
- MySQL 8.x accesible localmente con permisos para crear las bases `gtm` y `gtm_test`.
- Git y Bash/PowerShell para ejecutar los scripts.
- Opcional: Python 3 para `src/scripts/password_encrypt_gen.py`.
- Espacio en disco para el directorio de adjuntos definido por `app.upload.dir`.

## Instalación y configuración
1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/JaredMCC4/gtm-api.git
   cd gtm-api
   ```
2. **Configurar la base de datos MySQL**
   - Crea las bases vacías `gtm` y `gtm_test`, más un usuario con permisos (p. ej. `gtm_user` / contraseña segura). Flyway se encargará del esquema y los datos seed al arrancar la app o al correr `./mvnw flyway:migrate`.
   - Usa las URLs de ejemplo en `src/main/resources/application-example.properties` y `src/test/resources/application-example.properties` (`jdbc:mysql://localhost:3306/gtm` y `jdbc:mysql://localhost:3306/gtm_test`).
3. **Variables de entorno (.env)**
   - Copia `.env.example` a `.env` y actualiza las credenciales reales. La aplicación importa automáticamente este archivo (`spring.config.import=optional:file:.env[.properties]`).
   - Variables esperadas:
     ```properties
     DB_URL=jdbc:mysql://localhost:3306/gtm?sslMode=PREFERRED&allowPublicKeyRetrieval=true&serverTimezone=America/Costa_Rica
     DB_USERNAME=gtm_user
     DB_PASSWORD=cambia_esta_contraseña
     JWT_KEY=coloca_un_secreto_base64_u_ofusca_de_al_menos_32_bytes
     CORS_ALLOWED_ORIGINS=http://localhost:3000
     UPLOAD_DIR=F:/Portafolio/Gestor_Tareas/GTM/uploads
     ```
   - `CORS_ALLOWED_ORIGINS` se inyecta directamente en `SecurityConfig`, por lo que cualquier cambio en `.env` surte efecto inmediato.
4. **Archivos `application.properties`**
   - Las propiedades principales viven en `src/main/resources/application.properties` y delegan en `.env`. Si necesitas personalizar otras propiedades en despliegues, crea un perfil nuevo tomando como base `application-example.properties`.
   - Para pruebas, copia `src/test/resources/application-example.properties` a `src/test/resources/application.properties` y ajusta las credenciales.
5. **Instalar dependencias (opcional)**
   ```bash
   ./mvnw --batch-mode dependency:go-offline
   ```
6. **Migraciones y datos semilla**
   - Flyway (`spring.flyway.enabled=true`, `baseline-on-migrate=true`) se ejecuta durante el arranque para crear el esquema (`V1__create_initial_schema.sql`) y sembrar los roles/usuario admin (`V2__seed_initial_data.sql`). El paquete `db/migration/local` se usa solo para datos locales de desarrollo (ya está incluido en `spring.flyway.locations` por defecto).

## Archivos de configuración de ejemplo
- `.env.example`: valores de ejemplo para la aplicación principal.
- `.env.test.example`: valores sugeridos para ejecutar las pruebas integradas (`JWT_SECRET`, credenciales y URL de `gtm_test`).
- `src/main/resources/application-example.properties`: referencia con todas las propiedades principales y comentarios.
- `src/test/resources/application-example.properties`: homólogo para la capa de pruebas; incluye la URL de `gtm_test`, Flyway y rutas temporales de adjuntos.

## Seguridad y autenticación
- **Tokens JWT:** `SecurityConfig` define un `JwtDecoder` basado en `jwt.secret` (`JWT_KEY`). No se realiza validación de issuer/audience ni se acepta un IdP externo; los tokens válidos son los emitidos por `AuthServiceImpl` usando `JwtUtil`.
- **Reglas de autorización:**
  - Públicos: `/api/v1/auth/**`, `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**`.
  - ADMIN: `/api/v1/admin/**`, `/actuator/**` (excepto health).
  - USER: `/api/v1/tareas|etiquetas|subtareas|usuarios|adjuntos/**`.
- **Errores HTTP:** Las credenciales incorrectas o usuarios inactivos generan `BadCredentialsException`, traducido a 401 tal como se documenta en OpenAPI y en este README.
- **Refresh tokens:** almacenados en la tabla `refresh_tokens` con vigencia de 30 días; `/auth/logout` los marca como revocados.
- **Archivos adjuntos:** configurados mediante `app.upload.dir`, expuestos por `FileUploadConfig` y protegidos por las verificaciones de propiedad en `AdjuntoServiceImpl`.

## Ejecución de la aplicación
```bash
./mvnw spring-boot:run
```
O bien empaqueta y ejecuta el JAR:
```bash
./mvnw clean package
java -jar target/gtm-backend-1.0.0.jar
```
- Puerto por defecto: `server.port=2828` → `http://localhost:2828`.
- Prefijo principal: `/api/v1` (ej. `http://localhost:2828/api/v1/tareas`).
- Asegúrate de que `UPLOAD_DIR` exista o la aplicación lo podrá crear automáticamente.

## Ejecución de pruebas
- **Comando principal**
  ```bash
  ./mvnw clean verify -P integration-tests
  ```
  o usa `./run-tests.sh` para ejecutar el mismo ciclo y validar la generación de reportes JaCoCo.
- **Base de datos:** Las pruebas no usan H2 ni Testcontainers; reutilizan la conexión MySQL definida en `src/test/resources/application.properties` y la anotación `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`. Es indispensable tener una base `gtm_test` accesible.
- **FlywayMigrationTest:** resuelve las credenciales desde `application.properties` + entorno y aplica las migraciones reales; necesitas un servidor MySQL disponible.
- **Reportes:**
  - `target/site/jacoco-ut/index.html`
  - `target/site/jacoco-it/index.html`
  - `target/site/jacoco/index.html` (consolidado + XML para CI)
- **Dependencias declaradas:** existe `spring-boot-testcontainers`, pero aún no se ha habilitado ningún test con `@Testcontainers`; la documentación refleja este estado para evitar confusiones.

## Documentación de la API y documentos funcionales
- **Swagger/OpenAPI:** disponible en `http://localhost:2828/swagger` y el JSON en `http://localhost:2828/v3/api-docs`. `OpenApiConfig` define título, versión, descripción, contacto (`Jared Ch, jaredjosue888@gmail.com`), servidor (`http://localhost:2828`) y licencia (MIT).
- **Documentos adicionales:**
  - [tecnologias.md](tecnologias.md): decisiones tecnológicas.
  - [HELP.md](HELP.md): enlaces de referencia.
  - [src/scripts/password_encrypt_gen.py](src/scripts/password_encrypt_gen.py): script CLI para generar contraseñas BCrypt.

## Changelog
- El historial de cambios completo vive en [`CHANGELOG.md`](CHANGELOG.md). La versión actual es `1.0.0`.

## Migraciones de base de datos
- Scripts en `src/main/resources/db/migration`.
  - `V1__create_initial_schema.sql`: crea roles, usuarios, relaciones usuario-rol, tareas, subtareas, etiquetas, tabla puente `tarea_etiquetas`, adjuntos y refresh tokens con índices y restricciones.
  - `V2__seed_initial_data.sql`: inserta los roles `USER` y `ADMIN`, además de un usuario administrador (`GTM_ADMIN`).
- Scripts específicos para desarrollo local en `src/main/resources/db/migration/local` (por ejemplo `V2001__seed_datos_locales.sql`); no deben promoverse a entornos productivos.
- `spring.flyway.baseline-on-migrate=true` permite aplicar migraciones sobre bases existentes.
- `src/test/java/io/github/jaredmcc4/gtm/migration/FlywayMigrationTest.java` verifica que las migraciones se apliquen correctamente usando las propiedades de prueba.

## Licencia
Proyecto bajo licencia [MIT](LICENSE).

## Autor
Jared Ch ([@JaredMCC4](https://github.com/JaredMCC4)). Contacto disponible en `OpenApiConfig` (jaredjosue888@gmail.com).
