FROM openjdk:10-jdk-slim

MAINTAINER Carl-Philipp Harmant <cp.harmant@gmail.com>

## Build the app
COPY . /tmp/cryptax
WORKDIR /tmp/cryptax
RUN chmod +x run.sh && \
    chmod +x gradlew && \
      ./gradlew clean build && \
      mv build/cryptax-backend-1.0.0.jar /opt && \
      mv run.sh /opt && \
      rm -rf /tmp/* && \
      rm -rf ~/.gradle

WORKDIR /opt
#COPY build/cryptax-backend-1.0.0.jar /opt
#COPY run.sh /opt
#RUN chmod +x run.sh

## Entry point
EXPOSE 8080
ENTRYPOINT ["/opt/run.sh"]
