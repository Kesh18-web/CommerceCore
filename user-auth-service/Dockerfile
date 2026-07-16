FROM openjdk:17

WORKDIR /app

COPY target/user-auth-service.jar user-auth-service.jar

EXPOSE 9898

ENTRYPOINT ["java", "-jar", "user-auth-service.jar"]