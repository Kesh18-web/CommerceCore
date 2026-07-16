FROM openjdk:17

WORKDIR /app

COPY target/notification-delivery-service.jar notification-delivery-service.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "notification-delivery-service.jar"]