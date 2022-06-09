#!/bin/bash

set -e

pushd services
./gradlew clean build
popd

# Read current version from version file if not present in env
if [[ -z "${VERSION}" ]]; then
  VERSION=$(cat ./services/version)
fi

# Manually build info service with mcl

# info service
DEPENDENCY_PATH=services/info/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.info.InfoApplication \
  -t cryptimeleon/incentive-service-info:$VERSION \
  -f services/Dockerfile .

# promotion service
DEPENDENCY_PATH=services/inc/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.promotion.PromotionApplication\
  -t cryptimeleon/incentive-service-promotion:$VERSION \
  -f services/Dockerfile .

# basket service
DEPENDENCY_PATH=services/basket/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.basket.BasketApplication \
  -t cryptimeleon/incentive-service-basket:$VERSION \
  -f services/Dockerfile .

# bootstrap service
DEPENDENCY_PATH=services/bootstrap/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.bootstrap.BootstrapApplication \
  -t cryptimeleon/incentive-service-bootstrap:$VERSION \
  -f services/Dockerfile .
