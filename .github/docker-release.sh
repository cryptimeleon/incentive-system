#!/bin/bash

set -e

SERVICES=( issue credit basketserver )
VERSION=$(echo "$SOURCE_TAG" | cut -c 2-)  # Remove v from version
echo "Building and deploying docker images with version: $VERSION"

for SERVICE in "${SERVICES[@]}"
do
  IMAGE=cryptimeleon/incentive-service-${SERVICE}

  echo "Building docker images for ${SERVICE}-service."

  ./gradlew ":${SERVICE}:bootBuildImage"

  echo "Uploading docker images for ${SERVICE}-service."
  # Login to dockerhubwith credentials
  echo "$DOCKER_ACCESS_TOKEN" | docker login -u "$DOCKER_USERNAME" --password-stdin

  # Push docker image to dockerhub
  docker push "${IMAGE}:${VERSION}"

  echo "Finished deploying ${SERVICE}-service!"
done

echo "All services deployed successfully!"
