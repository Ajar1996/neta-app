FROM openjdk:8-jdk-alpine
VOLUME /tmp
ENV TimeZone=Asia/Shanghai
ARG neta-app
ENV CATALINA_HOME /usr/local/tomcat
ENV LANG en_US.UTF-8
ENV LC_ALL en_US.UTF-8

#设置时区
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
  && echo 'Asia/Shanghai' >/etc/timezone \

COPY neta-app.jar neta-app.jar
ENTRYPOINT ["java","-Dspring.output.ansi.enabled=never","-jar","/neta-app.jar"]