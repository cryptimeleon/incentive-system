#!/bin/bash

pushd ../math
sdk use java

pushd ../incentive-system
sdk use java 16-open
./gradlew build
./gradlew cryptoprotocol:publishToMavenLocal
popd


