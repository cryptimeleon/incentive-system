#!/bin/bash

JARS=(info store incentive dsprotection)

# shellcheck disable=SC2164
pushd services
for JAR in "${JARS[@]}"; do
  java -jar $JAR/build/libs/$JAR.jar &
done
