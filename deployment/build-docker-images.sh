#!/bin/bash

set -e

pushd services
./gradlew clean build
popd

VERSION="latest"

# info service
DEPENDENCY_PATH=services/info/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.info.InfoApplication \
  -t cptml/incsys-info:$VERSION \
  -f services/Dockerfile .

# provider service
DEPENDENCY_PATH=services/provider/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.provider.ProviderApplication\
  -t cptml/incsys-provider:$VERSION \
  -f services/Dockerfile .

# store service
DEPENDENCY_PATH=services/store/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.store.StoreApplication \
  -t cptml/incsys-store:$VERSION \
  -f services/Dockerfile .

# bootstrap service
DEPENDENCY_PATH=services/bootstrap/build/dependency
mkdir -p $DEPENDENCY_PATH && (cd $DEPENDENCY_PATH; jar -xf ../libs/*.jar)
docker build \
  --build-arg DEPENDENCY=$DEPENDENCY_PATH \
  --build-arg APPLICATION=org.cryptimeleon.incentive.services.bootstrap.BootstrapApplication \
  -t cptml/incsys-bootstrap:$VERSION \
  -f services/Dockerfile .

pushd web
docker build -t cptml/incsys-web:$VERSION .
popd
