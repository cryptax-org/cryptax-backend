#!/bin/bash
set -ev
bundle exec rake:units
if [[ "$TRAVIS_BRANCH" == "develop" ]]; then
    docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD ;
    docker push cryptaxapp/cryptax-backend:latest ;
fi
