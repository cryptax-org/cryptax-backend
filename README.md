# cryptax-backend

[![Build Status](https://travis-ci.org/cryptax-org/cryptax-backend.svg?branch=master)](https://travis-ci.org/cryptax-org/cryptax-backend) 
[![codecov](https://codecov.io/gh/cryptax-org/cryptax-backend/branch/master/graph/badge.svg)](https://codecov.io/gh/cryptax-org/cryptax-backend)

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

`java -Djasypt.encryptor.password=yourpassword -jar build/cryptax-backend-1.0.0.jar`

### Run in debug mod

`java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n -jar build/cryptax-backend-1.0.0.jar`

### Jasypt

Password are encrypted with Jasypt. To enrypt your password, use that command:

```
java -cp jasypt-1.9.2.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input=YourDBPassword password=yourpassword algorithm=PBEWithMD5AndDES
```

Then you just have to start the app with:

```
-Djasypt.encryptor.password=yourpassword
```

### Run with Docker

`docker-compose build && docker-compose up`

### Reports

`./gradlew testReport jacocoRootReport`
