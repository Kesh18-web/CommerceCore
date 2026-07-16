FROM openjdk:17

WORKDIR /app

COPY target/sales-checkout-service.jar sales-checkout-service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "sales-checkout-service.jar"]