FROM openjdk:8-jre-alpine

COPY build/libs/code-challenge-1.0-SNAPSHOT-all.jar /app.jar

CMD ["/usr/bin/java", "-jar", "/app.jar"]
