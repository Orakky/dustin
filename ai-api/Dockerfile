FROM openjdk:8-alpine
LABEL authors="orakky"

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone

COPY target/ai-api.jar ai-api.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","ai-api.jar"]
