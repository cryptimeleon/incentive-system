#!/bin/bash

pushd deployment

export PROVIDER_SHARED_SECRET=$(./util.sh)
export BASKET_PROVIDER_SECRET=$(./util.sh)
export BASKET_PROVIDER_SECRET_TWO=$(./util.sh)
export INCENTIVE_PROVIDER_SECRET=$(./util.sh)
export STORE_SHARED_SECRET=$(./util.sh)
docker-compose -f docker-compose-nginx.yaml up -d
