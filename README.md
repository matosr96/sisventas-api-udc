# SisVentas — API de gestión de ventas

[![Java CI/CD](https://github.com/matosr96/sisventas-api-udc/actions/workflows/ci.yml/badge.svg)](https://github.com/matosr96/sisventas-api-udc/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

API REST para un punto de venta pequeño: catálogo de productos, ventas con control de stock
y trazabilidad de quién hizo qué.

Es un **proyecto personal**. No está desplegado en ningún sitio y no atiende a nadie: su razón
de ser es servir de referencia de un estándar de arquitectura concreto —un archivo por
operación, capas con un solo motivo de cambio, errores como códigos de dominio, esquema
gobernado por migraciones escritas a mano— aplicado de principio a fin en un dominio que
duele si se modela mal, el de facturar.

## Qué hace

- **Catálogo**: productos con SKU único, precio de compra y venta, y categorías.
- **Ventas con líneas**: cada venta guarda qué se vendió, cuántas unidades y **a qué precio se
  vendió entonces**. Subir un precio no altera las facturas ya emitidas.
- **Stock real**: vender descuenta unidades en la misma transacción y rechaza la venta si no
  hay existencias; anularla las devuelve.
- **Correlativo de factura** por año (`F-2026-000001`).
- **Usuarios y roles** (`USER`, `ADMIN`) con JWT y una única matriz de autorización.
- **Auditoría automática** de toda escritura exitosa, sin que las rutas hagan nada.
- **Contratos uniformes**: paginación `{ count, page, pages, items }` y errores
  `{ "message": "<código>" }` en toda la API.
- **OpenAPI/Swagger** en `/swagger-ui.html`.

### Lo que todavía no hace

Para que el alcance quede claro:

- No modela **reposiciones ni compras a proveedor**: el stock solo baja al vender y sube al
  anular. Por eso `initialStock` es un dato del alta y no se actualiza.
- No **imprime** facturas: hay número, líneas y totales, pero no PDF ni plantilla.
- No tiene **gestión de usuarios por API** más allá del alta: los roles se asignan en la base.
- No hay **despliegue**: el CI construye la imagen Docker pero nadie la publica.

## Arranque rápido

Con Docker, que trae MySQL:

```bash
docker compose up -d db
./scripts/db-migrate.sh apply
./mvnw spring-boot:run
```

Sin Docker, con un MySQL 8 propio:

```bash
export DB_URL="jdbc:mysql://localhost:3306/bdsisventas"
export DB_USERNAME=root DB_PASSWORD=...
export JWT_SECRET="una_cadena_de_al_menos_32_caracteres"
./scripts/db-migrate.sh apply
./mvnw spring-boot:run
```

Comprobar que responde y sacar un token:

```bash
curl -s localhost:8080/actuator/health

TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Edgar","lastName":"Matos","username":"matos","password":"12345678"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["accessToken"])')

curl -s localhost:8080/api/v1/products -H "Authorization: Bearer $TOKEN"
```

El usuario nace con rol `USER`: puede leer y registrar ventas. Para tocar el catálogo hace
falta `ADMIN`, que se asigna en la tabla `users_roles`.

La configuración va por entorno, nunca en el repositorio: `DB_URL`, `DB_USERNAME`,
`DB_PASSWORD`, `JWT_SECRET` (mínimo 32 caracteres), `CORS_ORIGINS`, `PORT`.

## Stack

Java 17 · Spring Boot 3.2 · Spring Security con JWT (jjwt 0.11.5) · Spring Data JPA · MySQL 8 ·
springdoc-openapi · Lombok.

Herramientas: Maven (wrapper incluido), Docker Compose, GitHub Actions, Checkstyle, JaCoCo.

Requisitos: Java 17+ y MySQL 8, o solo Docker.

## Estructura del proyecto

Arquitectura por capas con **un archivo por operación**, siguiendo el estándar de Orienta
(`.claude/rules/convenciones.md`). El código va en inglés; los comentarios y la documentación, en español.

```
src/main/java/com/api/sisventas/
├── server/                 # Transversal: GlobalErrorHandler, AuditFilter, OpenApiConfig
├── security/               # JWT, CORS y la matriz de autorización (SecurityConfig)
├── routes/<entity>/        # Capa HTTP: CreateProductRoute, ListProductsRoute, ...
├── businessLogic/<entity>/ # Dominio: CreateProduct, ListProducts, ... (método execute)
├── models/                 # Entidades JPA
│   └── dtos/<entity>/      # Contrato de la API: *Request y *Response (records)
├── dataSources/            # Repositorios Spring Data + SqlErrors
└── common/                 # DomainError, ErrorCodes, PaginatedResponse, Pagination, Authenticated

migraciones/                # SQL numerado escrito a mano (ddl-auto=validate)
scripts/db-migrate.sh       # Aplica y audita las migraciones
```

Reglas que sostienen la estructura:

- La entidad JPA **nunca** cruza la frontera HTTP: entra un `*Request`, sale un `*Response`.
- Las rutas no tienen `try/catch`: `GlobalErrorHandler` traduce todo error al mismo contrato.
- La autorización vive solo en `SecurityConfig`; toda ruta nueva nace protegida.
- El esquema lo gobiernan las migraciones, nunca Hibernate.

## Base de datos y migraciones

El esquema se escribe a mano en `migraciones/` y `spring.jpa.hibernate.ddl-auto=validate`: si una
entidad y su tabla no coinciden, la aplicación no arranca.

```bash
./scripts/db-migrate.sh status    # qué está aplicado y qué falta
./scripts/db-migrate.sh apply     # aplica lo pendiente, en orden
```

Una base que ya tenía el esquema anterior debe marcar la primera migración antes del primer `apply`:

```bash
./scripts/db-migrate.sh baseline 0001_esquema_inicial.sql
```

## Documentación de la API

La API REST está disponible en `http://localhost:8080/api/v1`:

- Autenticación mediante JWT (`Authorization: Bearer <token>`)
- Respuestas en formato JSON con campos en `camelCase`
- Documentación OpenAPI/Swagger en `/swagger-ui.html`

### Contratos comunes

Listados paginados — `?page=` (base 1) y `?limit=` (por defecto 10):

```json
{ "count": 42, "page": 1, "pages": 5, "items": [ ... ] }
```

Errores — el mismo cuerpo siempre, con el código de dominio como mensaje:

```json
{ "message": "601" }
```

### Códigos de error

| Código | Significado | HTTP |
| --- | --- | --- |
| 601 | Producto no encontrado | 404 |
| 602 | Categoría no encontrada | 404 |
| 603 | Venta no encontrada | 404 |
| 604 | Usuario no encontrado | 404 |
| 610 | El nombre de usuario ya existe | 409 |
| 611 | Credenciales inválidas | 401 |
| 612 | Falta el rol por defecto (`USER`) en la base | 500 |
| 613 | Sin permisos para este recurso | 403 |
| 620 | La categoría tiene productos y no se puede eliminar | 409 |
| 621 | Stock insuficiente para la venta | 409 |
| 622 | El SKU ya existe | 409 |
| 623 | El nombre de categoría ya existe | 409 |
| 630 | Parámetros de paginación inválidos | 400 |
| 631 | Petición inválida (falla la validación del DTO) | 400 |
| 690 | Violación de integridad referencial | 409 |
| 699 | Error interno no controlado | 500 |

Añadir un código implica tocar `common/ErrorCodes` y esta tabla en el mismo commit.

### Endpoints

#### Autenticación (públicos)
- `POST /api/v1/auth/signup`: Registra un usuario con rol `USER` y devuelve su token
- `POST /api/v1/auth/signin`: Autentica y devuelve el token de acceso

#### Productos
- `GET /api/v1/products`: Listar productos (paginado) — autenticado
- `GET /api/v1/products/{id}`: Obtener producto — autenticado
- `POST /api/v1/products`: Crear producto — `ADMIN`
- `PUT /api/v1/products/{id}`: Actualizar producto (parcial) — `ADMIN`
- `DELETE /api/v1/products/{id}`: Eliminar producto — `ADMIN`

#### Categorías
- `GET /api/v1/categories`: Listar categorías (paginado) — autenticado
- `GET /api/v1/categories/{id}`: Obtener categoría — autenticado
- `POST /api/v1/categories`: Crear categoría — `ADMIN`
- `PUT /api/v1/categories/{id}`: Actualizar categoría (parcial) — `ADMIN`
- `DELETE /api/v1/categories/{id}`: Eliminar categoría — `ADMIN`

#### Ventas
- `GET /api/v1/sales`: Listar ventas (paginado) — autenticado
- `GET /api/v1/sales/{id}`: Obtener venta con sus líneas — autenticado
- `POST /api/v1/sales`: Registrar venta — `USER` o `ADMIN`
- `PUT /api/v1/sales/{id}`: Corregir **solo la fecha** — `USER` o `ADMIN`
- `DELETE /api/v1/sales/{id}`: Anular la venta y devolver el stock — `ADMIN`

Una venta se registra con sus líneas; el precio y el total los pone el servidor:

```json
POST /api/v1/sales
{ "items": [ { "productId": 1, "quantity": 3 } ] }
```

```json
{
  "id": 1,
  "saleNumber": "F-2026-000001",
  "saleDate": "2026-09-18T14:37:21.153220Z",
  "total": 7502.25,
  "items": [
    { "productId": 1, "productSku": "COCA-350", "quantity": 3,
      "unitPrice": 2500.75, "subtotal": 7502.25 }
  ]
}
```

## Reglas del dominio

Decisiones del modelo que no son evidentes leyendo solo los endpoints:

- **Una venta es inmutable salvo su fecha.** `unitPrice` se congela al registrar, así que
  subir el precio de un producto no altera las facturas ya emitidas. Para rectificar una
  venta se anula (lo que devuelve el stock) y se registra de nuevo.
- **El total lo calcula el servidor** sumando los subtotales. El cliente no lo envía.
- **Vender descuenta stock** dentro de la misma transacción: una línea sin stock deshace la
  venta entera con el código 621.
- **El dinero es `DECIMAL(12,2)`**, nunca coma flotante.
- **Un producto vendido no se borra, se desactiva** (`status: INACTIVE`): borrarlo destruiría
  las líneas de venta que lo referencian. El borrado real queda para lo que nunca se usó.
- **Una categoría con productos no se borra** (código 620).
- **`createdBy` es trazabilidad, no propiedad**: nadie filtra por él. Todo usuario autenticado
  ve el catálogo completo.
- **`saleDate` es la fecha del negocio** y puede ser retroactiva; `createdAt` es cuándo se
  registró en el sistema.
- **`initialStock` es el stock del alta** y no se actualiza: el sistema todavía no modela
  reposiciones ni compras a proveedor.

Toda escritura exitosa queda registrada en la tabla `audits` con usuario, método y recurso.

## Pruebas

```bash
./mvnw test      # levanta el contexto contra H2 en memoria: no necesita MySQL ni Docker
./mvnw package   # compila, pasa Checkstyle y ejecuta las pruebas
./mvnw test jacoco:report   # cobertura en target/site/jacoco
```

La cobertura es mínima: hay una prueba de contexto y poco más. Las migraciones **no** se
ejecutan en las pruebas, así que un error en el SQL solo aparece al arrancar contra MySQL de
verdad, donde `ddl-auto=validate` compara el esquema con las entidades.

## Calidad y CI

Checkstyle corre en la fase `validate` de cada build y el proyecto está en cero avisos.
JaCoCo genera el informe de cobertura tras los tests. El plugin de SonarQube está declarado
en el `pom.xml` para quien quiera apuntarlo a un servidor propio (`./mvnw sonar:sonar`), pero
el CI no lo ejecuta.

El workflow de GitHub Actions compila con Maven, corre las pruebas, sube los resultados,
construye la imagen Docker y pasa un escaneo de seguridad con Snyk (requiere el secret
`SNYK_TOKEN`; sin él, ese paso falla).

## Documentación relacionada

- [docs/DOCKER.md](docs/DOCKER.md) — detalle de los contenedores.
- [docs/SWAGGER.md](docs/SWAGGER.md) — documentación de la API.
- [CHANGELOG.md](CHANGELOG.md) — registro de cambios.
- [CONTRIBUTING.md](CONTRIBUTING.md) — cómo proponer cambios.

## Licencia

MIT — ver [LICENSE](LICENSE).
