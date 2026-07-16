FROM openjdk:17

WORKDIR /app

COPY target/billing-transaction-service.jar billing-transaction-service.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "billing-transaction-service.jar"]