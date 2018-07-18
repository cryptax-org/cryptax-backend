# cryptax-backend

[![Build Status](https://travis-ci.org/cryptax-org/cryptax-backend.svg?branch=master)](https://travis-ci.org/cryptax-org/cryptax-backend) 
[![codecov](https://codecov.io/gh/cryptax-org/cryptax-backend/branch/master/graph/badge.svg)](https://codecov.io/gh/cryptax-org/cryptax-backend)

### Pre-requisite

Java 10

### Configuration

Create a `.env` file based on `.env.template`. Its contains your env variables including your mater password to encrypt the app passwords.

### Compile

`./gradlew clean build`

### Run

`java -Djasypt.encryptor.password=yourpassword -jar build/cryptax-backend-1.0.0.jar`

### Run in debug mod

`java -Djasypt.encryptor.password=yourpassword -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n -jar build/cryptax-backend-1.0.0.jar`

### Jasypt

Password are encrypted with Jasypt. To encrypt your password, use that command:

```
java -cp jasypt-1.9.2.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input=dbPassword password=yourMasterPassword algorithm=PBEWithMD5AndDES
```

Then you just have to start the app with:

```
-Djasypt.encryptor.password=yourpassword
```

### Run with Docker

`docker-compose build && docker-compose up`

### Reports

`./gradlew testReport jacocoRootReport`
