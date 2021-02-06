#!/bin/bash

set -e

./gradlew clean build
./gradlew ":credit:bootBuildImage"
./gradlew ":issue:bootBuildImage"
./gradlew ":basketserver:bootBuildImage"
