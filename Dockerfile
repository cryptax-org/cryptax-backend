FROM openjdk:10-jdk-slim

MAINTAINER Carl-Philipp Harmant <cp.harmant@gmail.com>

## Build the app
COPY . /tmp/cryptax
WORKDIR /tmp/cryptax
RUN chmod +x gradlew && \
      ./gradlew clean build && \
      mv build/cryptax-backend-1.0.0.jar /opt && \
      rm -rf /tmp/* && \
      rm -rf ~/.gradle

WORKDIR /opt

## Entry point
EXPOSE 8080
ENTRYPOINT ["java","-jar","cryptax-backend-1.0.0.jar"]
