#!/bin/bash
set -ev
tag=$(if [ "$TRAVIS_BRANCH" == "develop" ]; then echo "$TRAVIS_BRANCH-$TRAVIS_COMMIT"; elif [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; fi)
if [ "$TRAVIS_BRANCH" == "develop" ] || [ "$TRAVIS_BRANCH" == "master" ]; then
    docker build    --build-arg BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"` \
                    --build-arg COMMIT_ID=$TRAVIS_COMMIT \
                    --tag cryptaxapp/cryptax-backend:$tag .
fi
#if [ "$TRAVIS_BRANCH" == "develop" ]; then
#    docker build --build-arg BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"` \
#                 --build-arg COMMIT_ID=$TRAVIS_COMMIT \
#                 --tag cryptaxapp/cryptax-backend:$TRAVIS_BRANCH-$TRAVIS_COMMIT .
#elif [ "$TRAVIS_BRANCH" == "master" ]; then
#    docker build --build-arg BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"` \
#                 --build-arg COMMIT_ID=$TRAVIS_COMMIT \
#                 --tag cryptaxapp/cryptax-backend:latest .
#fi
