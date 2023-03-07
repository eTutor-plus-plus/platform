FROM openjdk:21-slim

RUN mkdir /home/app
WORKDIR /home/app
COPY target/platform-0.0.1-SNAPSHOT.jar .
COPY src/main/docker/application/application.yml .
COPY src/main/docker/application/application-prod.yml .

CMD ["java", "-Xmx6g", "-Xms512m", "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED", "-Dspring.profiles.active=prod", "-Dspring.config.location=application.yml, application-prod.yml", "-jar", "platform-0.0.1-SNAPSHOT.jar"]
