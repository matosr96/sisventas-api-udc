# Registro de Cambios

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [No Publicado]

### Añadido
- Ventas con líneas (`sale_items`): cantidad y precio unitario congelado en el momento de la
  venta, con subtotal por línea.
- Control de stock real: vender descuenta unidades y rechaza la venta sin existencias
  (código 621); anularla las devuelve.
- Correlativo de factura por año (`F-2026-000001`), con contador propio y bloqueo pesimista.
- SKU único por producto y nombre único por categoría.
- Auditoría automática de toda escritura exitosa (`AuditFilter`, tabla `audits`).
- Paginación uniforme `{ count, page, pages, items }` y códigos de error de dominio.
- Migraciones SQL escritas a mano en `migraciones/` con `scripts/db-migrate.sh`.

### Cambiado
- Arquitectura por capas con un archivo por operación: `routes/`, `businessLogic/`, `models/`,
  `dataSources/`, `server/`, `security/`, `common/`. Código en inglés.
- La entidad JPA deja de cruzar la frontera HTTP: entra un `*Request`, sale un `*Response`.
- El total de la venta lo calcula el servidor; antes lo enviaba el cliente sin validación.
- El dinero pasa de `Double`/`Integer` a `BigDecimal` y `DECIMAL(12,2)`.
- La autorización vive solo en `SecurityConfig`; el filtro JWT ya no decide permisos.
- URLs por recurso (`POST /api/v1/products`), sin `/create`, `/getAll` ni `/listarId`.
- `products.user` y `categories.user` pasan a `created_by`: trazabilidad, no propiedad.
- El esquema lo gobiernan las migraciones con `ddl-auto=validate`.
- README reorientado al proyecto que es y traducido al inglés.

### Corregido
- Error de compilación en el antiguo `RestProducts`, que devolvía `ResponseEntity<Void>` con un
  cuerpo `String` y rompía `mvn package`.
- El DTO de autenticación devolvía el hash de la contraseña del usuario.
- Interpolación inválida en `docker-compose.yml` (`${VAR:default}` en vez de `${VAR:-default}`).
- `HELP.md` figuraba en `.gitignore` pero seguía rastreado, así que la regla no surtía efecto.
- `docs/SWAGGER.md` apuntaba a `/api/auth/login`, un endpoint que ya no existe.
- `docs/DOCKER.md` describía dos contenedores y omitía el paso de migraciones, sin el cual la
  aplicación no arranca.

### Eliminado
- `sales_count`, un contador que nadie incrementaba.
- Los DTOs duplicados `AuthResponseDto` y `DtoAuthRespuesta`.
- La relación `@ManyToMany` entre ventas y productos, sustituida por `sale_items`.

### Seguridad
- Se purgó del historial de git una contraseña de MySQL de desarrollo
  (`target/classes/application.properties`) y los archivos de `.idea/`, que se habían
  versionado en el commit inicial.

## Estado inicial

- Configuración del proyecto Spring Boot con MySQL y seguridad JWT.
- Estructura base, Docker y Docker Compose, CI/CD con GitHub Actions.
- Gestión de usuarios, productos y ventas.
- Documentación básica del proyecto.
