#!/bin/bash

set -e

./gradlew clean build
./gradlew ":credit:bootBuildImage"
./gradlew ":issue:bootBuildImage"
./gradlew ":info:bootBuildImage"
./gradlew ":basketserver:bootBuildImage"
