FROM openjdk:8-jdk-alpine
VOLUME /tmp
ENV TimeZone=Asia/Shanghai
# 使用软连接，并且将时区配置覆盖/etc/timezone
RUN ln -snf /usr/share/zoneinfo/$TimeZone /etc/localtime && echo $TimeZone > /etc/timezone
ARG neta-app
COPY neta-app.jar neta-app.jar
ENTRYPOINT ["java","-jar","/neta-app.jar"]