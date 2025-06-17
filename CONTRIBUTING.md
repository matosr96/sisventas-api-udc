# Guía de Contribución

¡Gracias por tu interés en contribuir a SisVentas! Este documento proporciona las directrices y el proceso para contribuir al proyecto.

## Código de Conducta

Este proyecto y todos los que participan en él se rigen por nuestro Código de Conducta. Al participar, se espera que respetes este código.

## ¿Cómo Contribuir?

### 1. Reportar un Bug

Si encuentras un bug, por favor crea un issue con la siguiente información:
- Descripción clara y concisa del bug
- Pasos para reproducir el problema
- Comportamiento esperado
- Capturas de pantalla (si aplica)
- Versión del sistema operativo y navegador (si aplica)

### 2. Sugerir una Mejora

Las sugerencias de mejora son bienvenidas. Por favor, crea un issue con:
- Descripción detallada de la mejora
- Justificación de la mejora
- Posibles alternativas consideradas

### 3. Enviar un Pull Request

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Estándares de Código

- Sigue las convenciones de código de Java
- Añade comentarios para código complejo
- Escribe pruebas unitarias para nuevo código
- Mantén la cobertura de código por encima del 80%
- Asegúrate de que todas las pruebas pasen

### Estructura de Commits

Usa el siguiente formato para los mensajes de commit:
```
tipo(alcance): descripción

[cuerpo opcional]

[pie opcional]
```

Tipos:
- feat: Nueva característica
- fix: Corrección de bug
- docs: Cambios en documentación
- style: Cambios que no afectan el significado del código
- refactor: Cambios que no arreglan bugs ni añaden features
- test: Añadir o corregir pruebas
- chore: Cambios en el proceso de build o herramientas auxiliares

### Proceso de Revisión

1. Todos los PRs requieren al menos una revisión aprobatoria
2. Los revisores pueden solicitar cambios
3. Una vez aprobado, el PR será mergeado por un mantenedor

## Configuración del Entorno de Desarrollo

1. Clona el repositorio
2. Configura el entorno de desarrollo:
   ```bash
   ./mvnw clean install
   ```
3. Ejecuta la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

## Pruebas

- Ejecuta las pruebas unitarias: `./mvnw test`
- Ejecuta las pruebas de integración: `./mvnw verify`
- Verifica la cobertura: `./mvnw test jacoco:report`

## Documentación

- Mantén la documentación actualizada
- Usa JavaDoc para documentar clases y métodos
- Actualiza el README.md cuando sea necesario
- Documenta cambios importantes en CHANGELOG.md

## Contacto

Si tienes preguntas, por favor:
- Abre un issue
- Contacta al equipo de mantenimiento
- Únete a nuestro canal de Discord 