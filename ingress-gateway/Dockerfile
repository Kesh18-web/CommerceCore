FROM openjdk:17

WORKDIR /app

COPY target/ingress-gateway.jar ingress-gateway.jar

EXPOSE 9191

ENTRYPOINT ["java", "-jar", "ingress-gateway.jar"]