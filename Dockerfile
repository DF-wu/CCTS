#FROM openjdk:11
FROM azul/zulu-openjdk-alpine:17
MAINTAINER df, df@dfder.tw
WORKDIR /usr/src/app
# from outside to inside
COPY . .
ENTRYPOINT ["java","-Dspring.profiles.active=container","-jar","app.jar"]
