#!/usr/bin/env bash
set -e
set -o pipefail

echo "going to install everything into Kubernetes"

## Orders
## TODO remove

export PROJECT_ID=bootiful

function deploy() {
  MODULE=$1
  cd $(dirname $0)/../$MODULE
  ROOT_DIR=$(pwd)
  APP_NAME=$MODULE
  TAG_NAME=${1:-$(date +%s)}
  IMAGE_TAG="${GITHUB_SHA:-$(git rev-parse HEAD)}"
  echo $IMAGE_TAG
  GCR_IMAGE_NAME=gcr.io/${PROJECT_ID}/${APP_NAME}
  mvn -f pom.xml \
    clean spring-boot:build-image \
    -e -Dspring.profiles.active=production,cloud \
    -Dspring-boot.build-image.imageName=$GCR_IMAGE_NAME
  IMAGE_ID=$(docker images -q $GCR_IMAGE_NAME)
  docker tag "${IMAGE_ID}" ${GCR_IMAGE_NAME}:latest
  docker tag "${IMAGE_ID}" ${GCR_IMAGE_NAME}:${IMAGE_TAG}
  docker push ${GCR_IMAGE_NAME}:latest
  docker push ${GCR_IMAGE_NAME}:${IMAGE_TAG}

}

deploy customers
