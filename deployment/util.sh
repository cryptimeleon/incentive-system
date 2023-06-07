#!/bin/bash

function generateSecret() {
  export LC_ALL=C
  tr -dc A-Za-z0-9 </dev/urandom | head -c 128
  printf "\n"
}

generateSecret
