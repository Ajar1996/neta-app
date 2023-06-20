FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG neta-app
COPY neta-app.jar neta-app.jar
ENTRYPOINT ["java","-jar","/neta-app.jar"]