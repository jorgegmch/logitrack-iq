# ============================================
# Dockerfile — Backend LogiTrack IQ (Spring Boot)
# Build multi-stage: compila con Maven, corre con JRE liviano
# ============================================

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar solo el pom.xml primero para cachear las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el resto del codigo y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------------------------------------------

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
