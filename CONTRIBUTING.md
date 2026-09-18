# Contribuir

Este es un proyecto personal y no tiene colaboradores fijos, pero los issues y las
propuestas son bienvenidos.

## Antes de proponer un cambio

1. Lee el README: describe qué hace el sistema, qué no hace a propósito y las reglas del
   dominio que no se negocian (dinero en `DECIMAL`, ventas inmutables, stock como libro mayor).
2. El código va en inglés; los comentarios, el Javadoc y la documentación interna, en español.
3. Un archivo por operación en `routes/` y `businessLogic/`; la entidad JPA nunca cruza HTTP;
   los errores son códigos de dominio registrados en la tabla del README.
4. Todo cambio de esquema es una migración SQL nueva en `migrations/`, escrita a mano, con la
   entidad actualizada en el mismo commit.

## Verificar

```bash
./mvnw package        # Checkstyle + pruebas; debe quedar en cero avisos
docker compose up -d db && ./scripts/db-migrate.sh apply && ./mvnw spring-boot:run
```

Arrancar contra MySQL con `ddl-auto=validate` es la prueba de que entidades y esquema cuadran.

## Commits

Mensajes en inglés, en imperativo, con el porqué en el cuerpo cuando no sea obvio.
