#!/bin/bash

###############################################################################
# Definition of functions and colors                                          #
###############################################################################

# Colors
NOCOLOR='\033[0m'
RED='\033[0;31m'
GREEN='\033[0;32m'
ORANGE='\033[0;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
LIGHTGRAY='\033[0;37m'
DARKGRAY='\033[1;30m'
LIGHTRED='\033[1;31m'
LIGHTGREEN='\033[1;32m'
YELLOW='\033[1;33m'
LIGHTBLUE='\033[1;34m'
LIGHTPURPLE='\033[1;35m'
LIGHTCYAN='\033[1;36m'
WHITE='\033[1;37m'
DEFAULT_COLOR=${LIGHTBLUE}

get_internal_node_ip() {
  kubectl get nodes -l "$1" -o jsonpath='{.items[*].status.addresses[?(@.type=="InternalIP")].address}'
}

header() {
  echo ""
  sep
  msg "| $1"
  sep
}

sep() {
  msg "-------------------------------------------------------------------------------"
}

big-sep() {
  msg "==============================================================================="
}

msg() {
  echo -e "${DEFAULT_COLOR}$1${NOCOLOR}"
}


###############################################################################
# Start of the actual script                                                  #
###############################################################################

big-sep
msg "| Starting local deployment 🧙‍"
big-sep

# Create fresh kind cluster
header "Create fresh kind cluster"
kind delete clusters kind-t2
kind create cluster --name kind-t2 --config deployment/kind-t2-config.yaml

# Build and load docker images of services
header "Build and load images of services"
set -e
./gradlew clean build
./gradlew ":credit:bootBuildImage"
./gradlew ":issue:bootBuildImage"
kind load docker-image upbcuk/incentive-service-issue --name kind-t2
kind load docker-image upbcuk/incentive-service-credit --name kind-t2
set +e

# Print available images
header "Available container images:"
docker exec -it kind-t2-control-plane crictl images

# Deploy to kubernetes
# Create ingress
header "Create ingress"
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=300s
kubectl apply -f deployment/ingress.yaml

# Deploy services
header "Deploy services"
kubectl apply -f deployment/credit-service.yaml
kubectl apply -f deployment/issue-service.yaml
kubectl apply -f deployment/basketserver-service.yaml

echo ""
big-sep
msg "| Done! 🥳"
msg "| Node-ip: $(get_internal_node_ip "cuk.cs.upb.de/kind-control-plane")"
big-sep