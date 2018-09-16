#!/bin/sh
java -Djasypt.encryptor.password=$JASYPT_PASSWORD -Dspring.profiles.active=$PROFILE -jar cryptax-backend.jar
