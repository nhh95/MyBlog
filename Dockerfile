FROM openjdk:17-jdk-alpine
COPY build/libs/*.jar app.jar
EXPOSE 9070
ENV SPRING_CONFIG_ADDITIONAL_LOCATION=/config/application.yml
ENTRYPOINT ["java","-jar","/app.jar"]
