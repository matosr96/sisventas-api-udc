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

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── sisventas/
│   │           ├── controllers/    # Controladores REST
│   │           ├── models/         # Entidades y DTOs
│   │           ├── repositories/   # Repositorios JPA
│   │           ├── services/       # Lógica de negocio
│   │           └── security/       # Configuración de seguridad
│   └── resources/
│       └── application.properties  # Configuración
└── test/
    └── java/
        └── com/
            └── sisventas/
                └── tests/          # Pruebas unitarias e integración
```

## Documentación de la API

La API REST está disponible en `http://localhost:8080/api/v1` y sigue las siguientes convenciones:

- Autenticación mediante JWT
- Respuestas en formato JSON
- Códigos de estado HTTP estándar
- Documentación con OpenAPI/Swagger en `/swagger-ui.html`

### Endpoints Principales

#### Autenticación
- `POST /api/v1/auth/login`: Autenticación de usuarios
- `POST /api/v1/auth/refresh`: Renovación de token

#### Productos
- `GET /api/v1/products`: Listar productos
- `POST /api/v1/products`: Crear producto
- `GET /api/v1/products/{id}`: Obtener producto
- `PUT /api/v1/products/{id}`: Actualizar producto
- `DELETE /api/v1/products/{id}`: Eliminar producto

#### Facturas
- `GET /api/v1/invoices`: Listar facturas
- `POST /api/v1/invoices`: Crear factura
- `GET /api/v1/invoices/{id}`: Obtener factura
- `PUT /api/v1/invoices/{id}`: Actualizar factura
- `DELETE /api/v1/invoices/{id}`: Eliminar factura

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
