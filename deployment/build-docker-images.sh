#!/bin/bash

set -e

./gradlew clean build
./gradlew ":services:credit:bootBuildImage"
./gradlew ":services:issue:bootBuildImage"
./gradlew ":services:basket:bootBuildImage"

# ./gradlew ":services:info:bootBuildImage"
# Manually build info service with mcl
pushd ..
mkdir -p services/info/build/dependency && (cd services/info/build/dependency; jar -xf ../libs/*.jar)
docker build --build-arg DEPENDENCY=services/info/build/dependency -t cryptimeleon/info-service -f services/info/Dockerfile .
popd
