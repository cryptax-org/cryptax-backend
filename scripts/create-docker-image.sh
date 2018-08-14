#!/bin/bash
set -ev
tag=$(if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo "$TRAVIS_BRANCH-$TRAVIS_COMMIT"; fi)
docker build    --build-arg BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"` \
                --build-arg COMMIT_ID=$TRAVIS_COMMIT \
                --tag cryptaxapp/cryptax-backend:$tag .
