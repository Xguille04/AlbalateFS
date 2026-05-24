# ── Etapa 1: Compilar con Maven ────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom.xml y descargar dependencias (cacheado si no cambia el pom)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar (sin tests)
COPY src ./src
RUN mvn package -DskipTests -B

# ── Etapa 2: Imagen de ejecución (más ligera) ──────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
