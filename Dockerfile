FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/hosteldekho-1.0.0.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
