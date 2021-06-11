#!/bin/bash

pushd deployment

export SHARED_SECRET=$(./util.sh)
docker-compose up -d
