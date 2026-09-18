# Guía de Docker para SisVentas API

## Requisitos Previos

- Docker instalado
- Docker Compose instalado
- Git instalado

## Estructura de Contenedores

El `docker-compose.yml` define tres servicios:
1. **app**: la API (Spring Boot)
2. **db**: MySQL 8
3. **phpmyadmin**: cliente web para la base, en el puerto 8081

## Pasos para Ejecutar con Docker

### 1. Clonar el Repositorio
```bash
git clone https://github.com/matosr96/sisventas-api-udc.git
cd sisventas-api-udc
```

### 2. Configurar Variables de Entorno
El compose trae valores por defecto para desarrollo, así que el `.env` es opcional.
Para cambiarlos, crea un `.env` en la raíz (está en `.gitignore`):
```env
MYSQL_ROOT_PASSWORD=...
MYSQL_DATABASE=bdsisventas
MYSQL_USER=sisventas
MYSQL_PASSWORD=...
JWT_SECRET=una_cadena_de_al_menos_32_caracteres
CORS_ORIGINS=http://localhost:5173
```

### 3. Levantar la base y aplicar el esquema

El esquema NO lo crea la aplicación: `spring.jpa.hibernate.ddl-auto=validate`, así que si la
base está vacía el arranque falla con un `SchemaManagementException`. Hay que aplicar las
migraciones primero.

```bash
docker compose up -d db
./scripts/db-migrate.sh apply
```

### 4. Construir y ejecutar el resto
```bash
docker compose up --build
```

### 5. Verificar los Contenedores
```bash
docker compose ps
curl -s localhost:8080/actuator/health
```

## Acceso a los Servicios

- **API**: http://localhost:8080
- **MySQL**: localhost:3306
- **phpMyAdmin**: http://localhost:8081
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## Comandos Útiles

### Detener los Contenedores
```bash
docker-compose down
```

### Ver Logs
```bash
docker-compose logs -f
```

### Reiniciar un Contenedor
```bash
docker-compose restart nombre_contenedor
```

## Solución de Problemas

### 1. Problemas de Conexión
- Verifica que los puertos no estén en uso
- Asegúrate de que las variables de entorno sean correctas
- Revisa los logs de los contenedores

### 2. Problemas de Base de Datos
- `SchemaManagementException` al arrancar: faltan migraciones.
  `./scripts/db-migrate.sh status` y luego `apply`
- Verifica que la base de datos se haya creado correctamente
- Comprueba los permisos del usuario
- Revisa la conexión desde la API

### 3. Problemas de Construcción
- Limpia las imágenes y contenedores antiguos:
```bash
docker-compose down --rmi all
docker system prune -a
```

## Mantenimiento

### Actualizar la Aplicación
1. Detén los contenedores
2. Obtén los últimos cambios
3. Reconstruye y reinicia:
```bash
docker-compose down
git pull
docker-compose up --build
```

### Backup de la Base de Datos
```bash
docker exec -t nombre_contenedor_mysql mysqldump -u root -p nombre_base_datos > backup.sql
```

## Notas Adicionales

- Los datos de la base de datos persisten en el volumen `mysql-data`
- El servicio `app` construye la imagen desde el `Dockerfile`: un cambio en el código exige
  `docker compose up --build`, no hay recarga en caliente
- Los logs se pueden ver en tiempo real usando `docker-compose logs -f` 