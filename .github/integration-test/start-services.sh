#!/bin/bash

JARS=(info store provider)

# shellcheck disable=SC2164
pushd services
for JAR in "${JARS[@]}"; do
  java -jar $JAR/build/libs/$JAR.jar &
done
