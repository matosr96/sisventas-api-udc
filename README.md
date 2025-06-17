# UNIVERSIDAD DE CARTAGENA
## INGENIERÍA DE SOFTWARE - PROGRAMACIÓN ORIENTADA A OBJETOS
### TRABAJO COLABORATIVO CONTEXTUALIZADO

## Sistema de Gestión de Ventas (SisVentas)

## Descripción

SisVentas es un sistema de gestión de ventas que permite administrar productos, realizar compras y gestionar el inventario. El sistema está diseñado para manejar múltiples productos por factura y mantener un control detallado del stock.

### Características Principales

- Gestión de productos y categorías
- Control de inventario en tiempo real
- Generación de facturas
- Seguimiento de compras
- Gestión de usuarios y roles
- API RESTful para integración con otros sistemas

## Tecnologías Utilizadas

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security con JWT
- Spring Data JPA
- MySQL 8.x

### Dependencias Principales

- **Spring Boot Starter Data JPA**: Integración con JPA para persistencia de datos
- **Spring Boot Starter Security**: Implementación de seguridad y autenticación
- **Spring Boot Starter Web**: Desarrollo de aplicaciones web REST
- **MySQL Connector/J**: Conexión con base de datos MySQL
- **Lombok**: Reducción de código boilerplate
- **jjwt**: Manejo de JSON Web Tokens
- **jaxb-api**: Procesamiento de XML

## Requisitos del Sistema

- Java 17 o superior
- MySQL 8.x
- Maven 3.6.x o superior
- IDE compatible con Java (recomendado: IntelliJ IDEA o Eclipse)

## Configuración del Proyecto

1. Clona el repositorio:
```bash
git clone https://github.com/tu-usuario/sisventas-api-udc.git
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
mvn clean install
```

4. Ejecuta la aplicación:
```bash
mvn spring-boot:run
```

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── sisventas/
│   │           ├── controllers/
│   │           ├── models/
│   │           ├── repositories/
│   │           ├── services/
│   │           └── security/
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── sisventas/
                └── tests/
```

## Documentación de la API

La API REST está disponible en `http://localhost:8080/api/v1` y sigue las siguientes convenciones:

- Autenticación mediante JWT
- Respuestas en formato JSON
- Códigos de estado HTTP estándar

### Endpoints Principales

- `POST /api/v1/auth/login`: Autenticación de usuarios
- `GET /api/v1/products`: Listar productos
- `POST /api/v1/products`: Crear producto
- `GET /api/v1/invoices`: Listar facturas
- `POST /api/v1/invoices`: Crear factura

## Pruebas

Para ejecutar las pruebas unitarias:

```bash
mvn test
```

## Contribución

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## Contacto

Universidad de Cartagena - Facultad de Ingeniería
