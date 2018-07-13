# cryptax-backend

[![Build Status](https://travis-ci.org/cryptax-org/cryptax-backend.svg?branch=master)](https://travis-ci.org/cryptax-org/cryptax-backend)

### Pre-requisite

Java 10

```
> java --version
java 10 2018-03-20
Java(TM) SE Runtime Environment 18.3 (build 10+46)
Java HotSpot(TM) 64-Bit Server VM 18.3 (build 10+46, mixed mode)
```

### Compile

`./gradlew clean build`

### Run

`java -jar build/cryptax-backend-1.0.0.jar`

### Run in debug mod

`java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n -jar build/cryptax-backend-1.0.0.jar`

### Run with Docker

`docker-compose build && docker-compose up`

### Reports

`./gradlew testReport jacocoRootReport`
