#!/bin/bash

./gradlew :issue:bootRun :credit:bootRun :basketserver:bootRun --parallel --max-workers=4
