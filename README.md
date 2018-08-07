# cryptax-backend

[![Build Status](https://travis-ci.org/cryptax-org/cryptax-backend.svg?branch=master)](https://travis-ci.org/cryptax-org/cryptax-backend) 
[![codecov](https://codecov.io/gh/cryptax-org/cryptax-backend/branch/master/graph/badge.svg)](https://codecov.io/gh/cryptax-org/cryptax-backend)

### Pre-requisite

Java 10

### Configuration

Create a `.env` file based on `.env.template`. Its contains your env variables including your mater password to encrypt the app passwords.

### Compile

```
./gradlew clean build
```

### Run

```
java -Djasypt.encryptor.password=yourMasterPassword -jar build/cryptax-backend-1.0.0.jar
```

### Run in debug mod

```
java -Djasypt.encryptor.password=yourMasterPassword -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n -jar build/cryptax-backend-1.0.0.jar`
```

### Jasypt

Password are encrypted with Jasypt. To encrypt your password, use that command:

```
java -cp jasypt-1.9.2.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input=dbPassword password=yourMasterPassword algorithm=PBEWithMD5AndDES
```

Then you just have to start the app with:

```
-Djasypt.encryptor.password=yourMasterPassword
```

### Run with Docker

```
docker-compose build && docker-compose up
```

### Junit and Jacoco reports

```
./gradlew testReport jacocoRootReport
```

### Generate jwt keystore

```
keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass secret -keyalg EC -keysize 521 -alias ES512 -keypass secret -sigalg SHA512withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
```

### Google Cloud

To be able to run the app with Google Cloud, your environment needs to configured as described [here](https://cloud.google.com/docs/authentication/production)
