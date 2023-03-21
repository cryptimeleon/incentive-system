#!/bin/bash

set -e

SERVICES=(store provider info bootstrap web)
echo "Build docker images"
./deployment/build-docker-images.sh

for SERVICE in "${SERVICES[@]}"; do
  IMAGE=cptml/incsys-${SERVICE}

  echo "Pushing docker image for ${SERVICE}-service."
  # Login to dockerhub with credentials
  echo "$DOCKER_ACCESS_TOKEN" | docker login -u "$DOCKER_USERNAME" --password-stdin

  # Push docker image to dockerhub
  docker push "${IMAGE}:latest"
done

echo "All docker images pushed successfully!"
