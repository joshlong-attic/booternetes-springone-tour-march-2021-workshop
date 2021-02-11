#!/usr/bin/env bash
set -e
set -o pipefail

echo "going to install everything into Kubernetes"

## Orders
## TODO remove

export PROJECT_ID=bootiful
export START_DIR=$(cd $(dirname $0) && pwd)
export IMAGE_TAG="${GITHUB_SHA:-$(git rev-parse HEAD)}"
export ORDER_PORT=8083
export CUSTOMER_PORT=8084
export GATEWAY_PORT=8087

echo $IMAGE_TAG

function deploy() {
  MODULE=$1
  cd $START_DIR/../$MODULE
  pwd
  ROOT_DIR=$(pwd)
  APP_NAME=$MODULE
  TAG_NAME=${1:-$(date +%s)}
  GCR_IMAGE_NAME=gcr.io/${PROJECT_ID}/${APP_NAME}

  docker images -q $GCR_IMAGE_NAME | while read l; do
    docker rmi -f $l
  done

  mvn -f pom.xml \
    -DskipTests=true \
    clean spring-boot:build-image -e \
    -Dspring.profiles.active=production,cloud \
    -Dspring-boot.build-image.imageName=$GCR_IMAGE_NAME

  IMAGE_ID="$(docker images -q $GCR_IMAGE_NAME)"
  echo foudn the image id $IMAGE_ID
  docker tag "${IMAGE_ID}" ${GCR_IMAGE_NAME}:latest
  docker push ${GCR_IMAGE_NAME}:latest
}

#deploy orders
#deploy customers
#deploy gateway

## Deploy the Orders Module
MODULE=orders
PORT=$ORDER_PORT
docker run \
  --env SPRING_RSOCKET_SERVER_PORT=$PORT \
  -p $PORT:$PORT \
  --expose $PORT \
  --rm -d  \
  gcr.io/bootiful/$MODULE

## Deploy the Customers Module
MODULE=customers
PORT=$CUSTOMER_PORT
docker run \
  --env SERVER_PORT=$PORT \
  -p $PORT:$PORT \
  --expose $PORT \
  --rm -d  \
  gcr.io/bootiful/$MODULE

## Deploy the Gateway Module
MODULE=gateway
PORT=$GATEWAY_PORT
docker run \
  --env SERVER_PORT=$PORT \
  --env GATEWAY_CUSTOMERS_HOSTNAME_AND_PORT=http://host.docker.internal:$CUSTOMER_PORT \
  --env GATEWAY_ORDERS_HOSTNAME_AND_PORT=tcp://host.docker.internal:$ORDER_PORT \
  -p $PORT:$PORT \
  --expose $PORT \
  --rm \
  gcr.io/bootiful/$MODULE
