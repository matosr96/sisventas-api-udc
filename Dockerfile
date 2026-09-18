# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Primero solo el pom: las dependencias quedan en una capa que no cambia con cada edicion de codigo.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY config ./config
COPY src ./src
RUN mvn -B clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# curl para el HEALTHCHECK; la imagen jre no lo trae.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

# Usuario no-root
RUN useradd -m -u 1000 appuser
USER appuser

HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
# headless: OpenPDF usa java.awt y el contenedor no tiene pantalla.
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "app.jar"]
