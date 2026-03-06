FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY Cheqmate/pom.xml pom.xml
COPY Cheqmate/src src
RUN apt-get update && apt-get install -y maven && \
    mvn -f pom.xml package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
