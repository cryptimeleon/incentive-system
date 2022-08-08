#!/bin/bash

JARS=(info basket inc dsprotectionservice)

# shellcheck disable=SC2164
pushd services
for JAR in "${JARS[@]}"; do
  java -jar $JAR/build/libs/$JAR.jar &
done
