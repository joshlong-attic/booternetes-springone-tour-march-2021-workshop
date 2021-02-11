#!/usr/bin/env bash

echo "going to install everything into Kubernetes"

## Orders

function deploy() {
  module=$1
  #  image_tag="${bp_mode_lowercase}${github_sha:-}"
  gcr_image_name=gcr.io/${PROJECT_ID}/${module}
  image_name=${gcr_image_name}:${image_tag}
  echo "the module is " $module

  cd $module
  ls -la
  mvn spring-boot:build-image -Dimage.name=
}
