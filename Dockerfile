FROM openjdk:8-jdk-alpine
RUN apk add bash maven curl docker
COPY . /source
CMD cd /source && mvn spring-boot:run