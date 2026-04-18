FROM amazoncorretto:25-alpine

WORKDIR /app

COPY ./build/libs/saas-api-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
