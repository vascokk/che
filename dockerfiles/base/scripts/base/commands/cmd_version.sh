#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

help_cmd_version() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} version\n"
  text "\n"
  text "List installed and available versions of ${CHE_MINI_PRODUCT_NAME}"
  text "\n"
}

pre_cmd_version() {
  :
}

post_cmd_version() {
  :
}

cmd_version() {
  # Do not perform any logging in this method as it is runnable before the system is bootstrap
  echo ""
  text "Your CLI version is '%s'.\n" $(get_image_version)
  if is_initialized; then
    text "Your installed version is '%s'.\n" $(get_installed_version)
  else
    text "Your installed version is '<not-installed>'.\n"
  fi

  text "\n"

  if is_offline; then
    text "Available on DockerHub: offline mode\n"
  else  
    text "Available on DockerHub:\n"

    local VERSION_LIST_JSON=$(curl -s https://hub.docker.com/v2/repositories/${CHE_IMAGE_NAME}/tags/)
    local NUMBER_OF_VERSIONS=$(echo $VERSION_LIST_JSON | jq '.count')

    DISPLAY_LIMIT=10
    if [ $DISPLAY_LIMIT -gt $NUMBER_OF_VERSIONS ]; then 
      DISPLAY_LIMIT=$NUMBER_OF_VERSIONS
    fi

    COUNTER=0
    while [ $COUNTER -lt $DISPLAY_LIMIT ]; do
      TAG=$(echo $VERSION_LIST_JSON | jq ".results[$COUNTER].name")
      text "  ${TAG//\"}\n"
      let COUNTER=COUNTER+1 
    done

    if [ $NUMBER_OF_VERSIONS -gt $DISPLAY_LIMIT ]; then
      OLDER_VERSION=$(echo $VERSION_LIST_JSON | jq '.next')
      text "  See older versions at: $OLDER_VERSION\n"
    fi
  fi
}

