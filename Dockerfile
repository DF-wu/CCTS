#FROM openjdk:11
FROM azul/zulu-openjdk-alpine:17
WORKDIR /usr/src/app
# from outside to inside
COPY . .
ENTRYPOINT ["java","-jar","app.jar"]
