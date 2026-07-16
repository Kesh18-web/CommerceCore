FROM openjdk:17

WORKDIR /app

COPY target/discovery-registry-0.0.1-SNAPSHOT.jar /app/discovery-registry-0.0.1-SNAPSHOT.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "discovery-registry-0.0.1-SNAPSHOT.jar"]