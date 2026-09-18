# Registro de Cambios

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [No Publicado]

### Añadido (sistema completo)
- **Cobro**: método de pago (efectivo, tarjeta, transferencia), importe recibido y cambio,
  nombre de cliente opcional; subtotal, descuento e impuesto (tasa `SALES_TAX_RATE`, congelada
  en cada venta) guardados por separado del total. Códigos `626` (efectivo insuficiente) y
  `627` (descuento mayor que el subtotal).
- **Devoluciones parciales** (`POST /sales/{id}/returns`): correlativo `R-`, reingreso al stock
  con asiento `SALE_RETURN`, tope por línea (`629`); una venta con devoluciones no se anula (`628`).
- **Listados con filtros y orden en servidor** para todos los recursos (`search`, estado,
  categoría, proveedor, vendedor, método de pago, rango de fechas; `sort`/`dir` con lista blanca).
- **Reportes**: resumen de Inicio, informe de ventas (serie diaria, por vendedor, productos más
  vendidos con margen estimado) y cierre de caja por método de pago menos devoluciones.
- **Libro de stock global** (`GET /inventory/movements`) y **auditoría consultable**
  (`GET /audits`, ADMIN).
- **Usuarios**: alta por administrador con rol (`POST /users`), edición de datos propios y
  ajenos, reinicio de contraseña por administrador y cierre de sesión en todos los dispositivos
  (`users.token_version` dentro del JWT).
- **PDF**: desglose de totales y pago; formato tirilla de 80 mm (`?format=receipt`).
- `GET /settings` con nombre del negocio, moneda y tasa de impuesto. Zona horaria del negocio
  (`BUSINESS_TIME_ZONE`) para cortar los días de los reportes.
- Migración `0006_complete_pos.sql`.

### Corregido
- `SqlErrors.isUniqueViolation` solo reconocía el código 1062 de MySQL; ahora también el SQLState
  estándar `23505` (H2 en pruebas, PostgreSQL), así que un duplicado se traduce a su código de
  dominio en cualquier base.
- Un cuerpo que no es JSON, un campo con tipo equivocado, un parámetro ausente o un `{id}` que no
  es número respondían 500 (`699`) y se logueaban como error propio. Ahora son 400 con el código
  `631`, igual que un DTO que no pasa la validación. Test que lo cubre.

### Corregido (auditoría completa)
- **Carrera en el stock**: ventas simultáneas sobre el mismo producto leían el mismo saldo y
  cada una escribía el suyo (20 ventas sobre 10 unidades vendían 11 y dejaban 8). El libro
  carga ahora el producto con bloqueo de fila; test de concurrencia que lo garantiza.
- El primer documento del año en concurrencia fallaba por clave duplicada en el contador.
  Los contadores se siembran ahora de antemano (migración 0005, al arrancar y a diario), de
  modo que emitir un número es solo una lectura con bloqueo: crearlos en la transacción del
  documento provocaba deadlocks en MySQL.
- Borrar un producto fallaba con 690 en cuanto tenía su asiento `INITIAL`: ahora se
  desactiva si tiene asientos y solo se borra si nunca se movió.
- Se podía vender y comprar un producto `INACTIVE` (nuevo código 607).
- El registro no tenía límite de intentos; el limitador cubre `/signup` y libera memoria.
- `purchasePrice` pasa a ser el último costo pagado: cada compra lo actualiza.
- `sales.user_id`, `purchases.user_id` y `stock_movements.user_id` son `NOT NULL`
  (migración 0004). Fechas de venta y compra no pueden ser futuras.
- Swagger en inglés, como el README. `UserResponse` vive en `dtos/user`.
- Dependencias: fuera `modelmapper`, `slf4j-api` y `logback-classic` (sin uso o
  redundantes) y el plugin Sonar; springdoc 2.6.0, compatible con Boot 3.3.
- `docker compose up` espera a que MySQL esté sano y reinicia la API si cae; el
  Dockerfile cachea las dependencias en su propia capa y arranca headless.
- Seis tests HTTP nuevos: matriz de autorización, contrato 401/403, cuenta desactivada con
  token vigente, auditoría, PDF y límites de intentos.

### Añadido
- Inventario: proveedores, compras con líneas y costo congelado (`P-2026-000001`), libro
  mayor `stock_movements` por el que pasa toda variación de stock, y ajustes manuales con
  motivo. El stock deja de editarse a mano en `PUT /products`.
- Gestión de usuarios por la API: listado, detalle, roles, activar/desactivar, `me` y cambio
  de contraseña propia. Una cuenta `INACTIVE` no entra ni con un token vigente, y nadie
  puede bloquearse a sí mismo (614).
- Factura en PDF: `GET /sales/{id}/pdf` (OpenPDF, única dependencia añadida fuera de Spring).
- Publicación de la imagen en GHCR (`ghcr.io/matosr96/sisventas-api`) en cada build verde de
  `main` y en cada tag `v*`.
- Tests de integración `StockLedgerTest` y `UserManagementTest`.
- Códigos 605, 606, 614, 615, 616, 624 y 625.
- Ventas con líneas (`sale_items`): cantidad y precio unitario congelado en el momento de la
  venta, con subtotal por línea.
- Control de stock real: vender descuenta unidades y rechaza la venta sin existencias
  (código 621); anularla las devuelve.
- Correlativo de factura por año (`F-2026-000001`), con contador propio y bloqueo pesimista.
- SKU único por producto y nombre único por categoría.
- Auditoría automática de toda escritura exitosa (`AuditFilter`, tabla `audits`).
- Paginación uniforme `{ count, page, pages, items }` y códigos de error de dominio.
- Migraciones SQL escritas a mano en `migrations/` con `scripts/db-migrate.sh`.

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
