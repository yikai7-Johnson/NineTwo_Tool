FROM registry.cn-hangzhou.aliyuncs.com/glue/ubuntu2204jdk8:0.1

RUN echo "Asia/Shanghai" > /etc/timezone

VOLUME /tmp

ADD NinetwoToolboxServer-1.0-SNAPSHOT.jar app.jar

COPY application.yml /home/web/config/

RUN mkdir /home/ninetwo/file/

EXPOSE 8090

ENTRYPOINT ["nohup","java","-Djava.security.egd=file:/dev/./urandom","-jar","-Dspring.config.location=/home/web/config/","app.jar","&"]