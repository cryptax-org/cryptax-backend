#!/bin/bash
set -ev
tag=$(if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; elif [ "$TRAVIS_BRANCH" == "develop" ]; then echo "develop-latest"; else echo "$TRAVIS_BRANCH-$TRAVIS_COMMIT"; fi)
docker build --tag cryptaxapp/cryptax-backend:$tag .
