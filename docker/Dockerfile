# Stage 1: Build — compila l'applicazione
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
# Costruisce il fat-jar (un jar che contiene tutte le dipendenze)
RUN gradle buildFatJar --no-daemon
# Stage 2: Runtime — immagine più piccola per produzione
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8080
# Avvia il server
ENTRYPOINT ["java", "-jar", "app.jar"]
