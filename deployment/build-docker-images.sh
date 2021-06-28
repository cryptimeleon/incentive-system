#!/bin/bash

set -e

#./gradlew clean build

# Read current version from version file
VERSION=$(cat ./services/version)

# Manually build info service with mcl

# info service
DEPENDENCY_PATH=services/info/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.info.InfoApplication \
  -t cryptimeleon/incentive-service-info:$VERSION \
  -f services/Dockerfile .

# issue service
DEPENDENCY_PATH=services/issue/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.issue.IssueApplication \
  -t cryptimeleon/incentive-service-issue:$VERSION \
  -f services/Dockerfile .

# credit service
DEPENDENCY_PATH=services/credit/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.credit.CreditApplication \
  -t cryptimeleon/incentive-service-credit:$VERSION \
  -f services/Dockerfile .

# basket server does not require mcl
./gradlew ":services:basket:bootBuildImage"
