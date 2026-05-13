FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/my-market-app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]