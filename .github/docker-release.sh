#!/bin/bash

set -e

SERVICES=(issue credit basket)
VERSION=$(echo "$SOURCE_TAG" | cut -c 2-) # Remove v from version
echo "Building and deploying docker images with version: $VERSION"

echo "Build docker images"
export VERSION && ./deployment/build-docker-images.sh

for SERVICE in "${SERVICES[@]}"; do
  IMAGE=cryptimeleon/incentive-service-${SERVICE}

  echo "Uploading docker images for ${SERVICE}-service."
  # Login to dockerhub with credentials
  echo "$DOCKER_ACCESS_TOKEN" | docker login -u "$DOCKER_USERNAME" --password-stdin

  # Push docker image to dockerhub
  docker push "${IMAGE}:${VERSION}"

  echo "Finished deploying ${SERVICE}-service!"
done

echo "All services deployed successfully!"
