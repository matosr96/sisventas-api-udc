# Guía de Docker para SisVentas API

## Requisitos Previos

- Docker instalado
- Docker Compose instalado
- Git instalado

## Estructura de Contenedores

El proyecto utiliza dos contenedores principales:
1. **API**: Aplicación Spring Boot
2. **MySQL**: Base de datos

## Pasos para Ejecutar con Docker

### 1. Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/sisventas-api-udc.git
cd sisventas-api-udc
```

### 2. Configurar Variables de Entorno
Crea un archivo `.env` en la raíz del proyecto:
```env
MYSQL_ROOT_PASSWORD=tu_contraseña
MYSQL_DATABASE=sisventas
MYSQL_USER=sisventas
MYSQL_PASSWORD=tu_contraseña_usuario
```

### 3. Construir y Ejecutar los Contenedores
```bash
docker-compose up --build
```

### 4. Verificar los Contenedores
```bash
docker-compose ps
```

## Acceso a los Servicios

- **API**: http://localhost:8080
- **MySQL**: localhost:3306
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

- Los datos de la base de datos persisten en un volumen de Docker
- La aplicación se reinicia automáticamente si hay cambios en el código
- Los logs se pueden ver en tiempo real usando `docker-compose logs -f` 