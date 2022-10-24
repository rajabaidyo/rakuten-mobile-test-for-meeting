FROM maven:3.8.6-openjdk-18-slim

WORKDIR /app

COPY . .

RUN mvn clean -DskipTests

RUN mvn install -DskipTests

ENTRYPOINT ["java", "-jar","target/user-api-docker.jar"]

EXPOSE 8080
