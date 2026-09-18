# UNIVERSIDAD DE CARTAGENA
## INGENIERÍA DE SOFTWARE - PROGRAMACIÓN ORIENTADA A OBJETOS
### TRABAJO COLABORATIVO CONTEXTUALIZADO

# Sistema de Gestión de Ventas (SisVentas)

[![Java CI/CD](https://github.com/matosr96/sisventas-api-udc/actions/workflows/ci.yml/badge.svg)](https://github.com/matosr96/sisventas-api-udc/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

## Descripción

SisVentas es un sistema de gestión de ventas que permite administrar productos, realizar compras y gestionar el inventario. El sistema está diseñado para manejar múltiples productos por factura y mantener un control detallado del stock.

### Características Principales

- Gestión de productos y categorías
- Control de inventario en tiempo real
- Generación de facturas
- Seguimiento de compras
- Gestión de usuarios y roles
- API RESTful para integración con otros sistemas
- Autenticación y autorización con JWT
- Documentación de API con OpenAPI/Swagger

## Tecnologías Utilizadas

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security con JWT
- Spring Data JPA
- MySQL 8.x

### Herramientas de Desarrollo
- Maven
- Docker
- GitHub Actions
- SonarQube
- JaCoCo

### Dependencias Principales

- **Spring Boot Starter Data JPA**: Integración con JPA para persistencia de datos
- **Spring Boot Starter Security**: Implementación de seguridad y autenticación
- **Spring Boot Starter Web**: Desarrollo de aplicaciones web REST
- **MySQL Connector/J**: Conexión con base de datos MySQL
- **Lombok**: Reducción de código boilerplate
- **jjwt**: Manejo de JSON Web Tokens
- **jaxb-api**: Procesamiento de XML
- **springdoc-openapi**: Documentación de API
- **spring-boot-starter-validation**: Validación de datos

## Requisitos del Sistema

- Java 17 o superior
- MySQL 8.x
- Maven 3.6.x o superior
- Docker y Docker Compose (opcional)
- IDE compatible con Java (recomendado: IntelliJ IDEA o Eclipse)

## Configuración del Proyecto

### Desarrollo Local

1. Clona el repositorio:
```bash
git clone https://github.com/matosr96/sisventas-api-udc.git
cd sisventas-api-udc
```

2. Configura la base de datos:
   - Crea una base de datos MySQL
   - Configura las credenciales en `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sisventas
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

3. Compila el proyecto:
```bash
./mvnw clean install
```

4. Ejecuta la aplicación:
```bash
./mvnw spring-boot:run
```

### Usando Docker

1. Construye y ejecuta los contenedores:
```bash
docker-compose up -d
```

2. Accede a la aplicación:
   - API: http://localhost:8080
   - phpMyAdmin: http://localhost:8081

## Estructura del Proyecto

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

### Pruebas Unitarias
```bash
./mvnw test
```

### Pruebas de Integración
```bash
./mvnw verify
```

### Cobertura de Código
```bash
./mvnw test jacoco:report
```

## Calidad de Código

El proyecto utiliza varias herramientas para mantener la calidad del código:

- **Checkstyle**: Para mantener estándares de código
- **PMD**: Para análisis estático de código
- **JaCoCo**: Para cobertura de pruebas
- **SonarQube**: Para análisis de calidad

## CI/CD

El proyecto utiliza GitHub Actions para CI/CD. El pipeline incluye:

- Build con Maven
- Ejecución de pruebas
- Análisis de calidad con SonarQube
- Escaneo de seguridad
- Construcción de imagen Docker

## Contribución

Por favor, lee [CONTRIBUTING.md](CONTRIBUTING.md) para detalles sobre nuestro código de conducta y el proceso para enviarnos pull requests.

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## Contacto

Universidad de Cartagena - Facultad de Ingeniería

## Registro de Cambios

Ver [CHANGELOG.md](CHANGELOG.md) para una lista de cambios.
