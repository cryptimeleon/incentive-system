#!/bin/bash

set -e

./gradlew clean build
./gradlew ":services:credit:bootBuildImage"
./gradlew ":services:issue:bootBuildImage"
./gradlew ":services:basket:bootBuildImage"
