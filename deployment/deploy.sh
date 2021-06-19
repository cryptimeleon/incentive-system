#!/bin/bash

pushd deployment

export SHARED_SECRET=$(./util.sh)
export BASKET_REDEEM_SECRET=$(./util.sh)
export BASKET_PAY_SECRET=$(./util.sh)
docker-compose up -d
