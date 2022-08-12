#!/bin/bash

JARS=(info basket incentive dsprotection)

# shellcheck disable=SC2164
pushd services
for JAR in "${JARS[@]}"; do
  java -jar $JAR/build/libs/$JAR.jar &
done
