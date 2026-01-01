FROM maven:3.9-amazoncorretto-21 AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src src/
RUN mvn clean package -DskipTests

FROM bellsoft/liberica-openjdk-alpine:21
WORKDIR /app
COPY --from=builder /build/target/*.jar /app/pokemon.jar
EXPOSE 8080
EXPOSE 5005

ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "pokemon.jar"]
