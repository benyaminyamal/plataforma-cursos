# ============================================================
# Etapa 1: build con Maven + JDK 17
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Cache de dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Compilacion y empaquetado (se omiten los tests en la imagen; ya corren en CI)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ============================================================
# Etapa 2: imagen de ejecucion (solo JRE)
# ============================================================
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# Usuario sin privilegios
RUN groupadd -r spring && useradd -r -g spring spring

# Carpeta donde se montara el wallet de Oracle Cloud en tiempo de ejecucion
RUN mkdir -p /app/wallet && chown -R spring:spring /app

COPY --from=build /build/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring
EXPOSE 8080

# Healthcheck contra el endpoint de Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
