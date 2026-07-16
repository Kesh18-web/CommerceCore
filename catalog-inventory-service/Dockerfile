FROM openjdk:17

WORKDIR /app

COPY target/catalog-inventory-service.jar catalog-inventory-service.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "catalog-inventory-service.jar"]