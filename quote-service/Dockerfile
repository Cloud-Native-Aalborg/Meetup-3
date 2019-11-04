FROM maven:3.6.2-jdk-11-slim AS build

COPY pom.xml pom.xml

#download dependencies for caching
RUN mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.1.1:go-offline

#copy the rest and build it
COPY src src
RUN mvn package spring-boot:repackage -DskipTests

FROM openjdk:11

EXPOSE 8080

WORKDIR /application

COPY --from=build target/*.jar /application

CMD java -jar /application/meetup-3-quote-service.jar
