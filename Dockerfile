FROM openjdk:12-jdk-slim

LABEL maintainer="cp.harmant@gmail.com"

ARG JASYPT_PASSWORD
ARG PROFILE

ENV JASYPT_PASSWORD $JASYPT_PASSWORD
ENV PROFILE $PROFILE

## Build the app
COPY . /tmp/cryptax
WORKDIR /tmp/cryptax
RUN   chmod +x scripts/run.sh && \
      chmod +x gradlew && \
      ./gradlew clean build -Dorg.gradle.daemon=false -Dorg.gradle.configureondemand=false -Dorg.gradle.parallel=false && \
      mv build/cryptax-backend.jar /opt && \
      mv scripts/run.sh /opt && \
      rm -rf /tmp/* && \
      rm -rf ~/.gradle/*

WORKDIR /opt

## Entry point
EXPOSE 8080
ENTRYPOINT ["/opt/run.sh"]
