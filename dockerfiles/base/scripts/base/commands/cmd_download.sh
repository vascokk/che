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

help_cmd_download() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} download [PARAMETERS]\n"
  text "\n"
  text "Downloads Docker images required to execute ${CHE_MINI_PRODUCT_NAME}\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --force                           Uses 'docker rmi' and 'docker pull' to forcibly retrieve latest images\n"
  text "  --no-force                        Updates images if matching tag not found in local cache\n"
  text "  --pull                            Uses 'docker pull' to check for new remote versions of images\n"
  text "\n"
}

pre_cmd_download() {
  :
}

post_cmd_download() {
  :
}


cmd_download() {
  FORCE_UPDATE=${1:-"--no-force"}

  IFS=$'\n'
  for SINGLE_IMAGE in $IMAGE_LIST; do
    VALUE_IMAGE=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    if [[ $FORCE_UPDATE == "--force" ]] ||
       [[ $FORCE_UPDATE == "--pull" ]]; then
      update_image $FORCE_UPDATE $VALUE_IMAGE
    else
      update_image_if_not_found $VALUE_IMAGE
    fi
  done
}

