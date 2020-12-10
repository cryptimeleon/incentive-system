#!/bin/sh

cd ./issue

./gradlew build

# Check if exit code is not 0
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi

rm -rf build
exit