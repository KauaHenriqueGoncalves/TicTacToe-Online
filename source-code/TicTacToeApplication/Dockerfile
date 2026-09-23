FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests \
    && mvn -B dependency:copy-dependencies -DoutputDirectory=target/dependency
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/TicTacToe-0.0.1.jar ./app.jar
COPY --from=build /app/target/dependency ./dependency
EXPOSE 8080 8081
ENTRYPOINT ["java", "-cp", "app.jar:dependency/*", "com.tic.tac.toe.Application"]