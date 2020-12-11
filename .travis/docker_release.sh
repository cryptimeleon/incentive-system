#!/bin/bash

IMAGE=upbcuk/incentive-service-issue
VERSION=$(echo $TRAVIS_TAG | cut -c 2-)  # Remove v from version


echo "Building docker images with version: $VERSION"

./gradlew :issue::bootBuildImage

docker tag ${IMAGE}:latest ${IMAGE}:${VERSION}

# Login to dockerhubwith credentials
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

# Push docker image to dockerhub
docker push ${IMAGE}:${VERSION}
docker push ${IMAGE}:latest