#!/bin/bash

pushd deployment

export PROVIDER_SHARED_SECRET=$(./util.sh)
export BASKET_PROVIDER_SECRET=$(./util.sh)
export BASKET_PROVIDER_SECRET_TWO=$(./util.sh)
export INCENTIVE_PROVIDER_SECRET=$(./util.sh)
export STORE_SHARED_SECRET=$(./util.sh)

export HOST=incentimeleon.cryptimeleon.org
# Use this for local deployments or change the host to your server's url
# export HOST=localhost:8009

docker compose -f docker-compose.yaml up -d
