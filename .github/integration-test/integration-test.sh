#!/bin/bash

JARS=(info basket inc dsprotectionservice)

function start_services() {
  # shellcheck disable=SC2164
  pushd services
  for JAR in "${JARS[@]}"; do
    java -jar $JAR/build/libs/$JAR.jar &
  done
}

function kill_services() {
  for JAR in "${JARS[@]}"; do
    PID=$(pgrep -f $JAR.jar)
    if test ! -z "$PID"; then
      kill -- $PID
    fi
  done
}

kill_services
start_services
sleep 15

./gradlew :client:integrationTest

kill_services
wait
