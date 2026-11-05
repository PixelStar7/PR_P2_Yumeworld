FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copiar el wrapper y pom
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Limpiar posibles saltos de línea CRLF de Windows y dar permisos
RUN tr -d '\r' < mvnw > mvnw_unix && mv mvnw_unix mvnw && chmod +x mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Runtime ligero
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]