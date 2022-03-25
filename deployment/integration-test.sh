#!/bin/bash

# Make sure the script is executed from the root directory
[[ -e ./deployment/build-docker-images.sh ]] || {
  printf "Please cd into the root folder of the directory before running this script:\n   ...$ ./deployment/integration-test.sh\n"
  exit 1
}

# Build and start services
./deployment/build-docker-images.sh
docker-compose -f ./deployment/docker-compose-localhost.yaml up -d
sleep 20

# Run Tests
pushd services
./gradlew integrationtest
popd

# Shutdown services
# It might be useful to comment this out when working on the client-side code
docker-compose -f ./deployment/docker-compose-localhost.yaml down
