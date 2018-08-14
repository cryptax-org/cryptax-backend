FROM openjdk:10-jdk-slim

MAINTAINER Carl-Philipp Harmant <cp.harmant@gmail.com>

ARG BUILD_DATE
ARG COMMIT_ID

LABEL com.cryptax.build-date=$BUILD_DATE \
      com.cryptax.name="Cryptax Backend" \
      com.cryptax.commit-id=$COMMIT_ID

## Build the app
COPY . /tmp/cryptax
WORKDIR /tmp/cryptax
RUN   chmod +x scripts/run.sh && \
      chmod +x gradlew && \
      ./gradlew clean build -x test && \
      mv build/cryptax-backend-1.0.0.jar /opt && \
      mv scripts/run.sh /opt && \
      rm -rf /tmp/* && \
      rm -rf ~/.gradle

WORKDIR /opt

## Entry point
EXPOSE 8080
ENTRYPOINT ["/opt/run.sh"]
