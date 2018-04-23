FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD build/libs/Orcha-alpha-1.1.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]