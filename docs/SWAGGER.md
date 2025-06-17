# Documentación de la API con Swagger

## Acceso a la Documentación

La documentación de la API está disponible a través de Swagger UI. Hay dos formas de acceder:

### 1. Interfaz Swagger UI
- URL: `http://localhost:8080/swagger-ui.html`
- Esta interfaz proporciona una documentación interactiva donde puedes:
  - Ver todos los endpoints disponibles
  - Probar las operaciones directamente desde el navegador
  - Ver los modelos de datos y esquemas
  - Autenticarte usando JWT

### 2. Especificación OpenAPI
- URL: `http://localhost:8080/api-docs`
- Este endpoint proporciona la especificación OpenAPI en formato JSON
- Útil para generar documentación o integrar con otras herramientas

## Características de la Documentación

La documentación incluye:

- Descripción detallada de cada endpoint
- Parámetros requeridos y opcionales
- Ejemplos de respuestas
- Códigos de estado HTTP
- Esquemas de autenticación
- Modelos de datos

## Autenticación

Para probar endpoints protegidos:

1. Usa el endpoint `/api/auth/login` para obtener un token JWT
2. Haz clic en el botón "Authorize" en la parte superior de Swagger UI
3. Ingresa el token en el formato: `Bearer tu_token_jwt`
4. Ahora puedes probar los endpoints protegidos

## Notas Adicionales

- La documentación se actualiza automáticamente con los cambios en el código
- Los ejemplos de respuestas son generados automáticamente
- Puedes descargar la especificación OpenAPI para uso externo 