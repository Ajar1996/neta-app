FROM openjdk:8-jdk-alpine
VOLUME /tmp

ENV TimeZone=Asia/Shanghai

# 安装 tzdata 包并设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TimeZone /etc/localtime && \
    echo "$TimeZone" > /etc/timezone \

ARG neta-app
ENV CATALINA_HOME /usr/local/tomcat
ENV LANG en_US.UTF-8
ENV LC_ALL en_US.UTF-8



COPY neta-app.jar neta-app.jar
ENTRYPOINT ["java","-Dspring.output.ansi.enabled=never","-jar","/neta-app.jar"]