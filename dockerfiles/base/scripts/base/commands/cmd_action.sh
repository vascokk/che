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

help_cmd_action() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} action <ACTION_NAME> [ACTION_PARAMETERS]\n"
  text "\n"
  text "Executes a REST action against ${CHE_MINI_PRODUCT_NAME} server or workspace.\n"
  text "\n"
  text "ACTIONS:\n"
  text "  create-start-workspace\n"
  text "  add-user\n"
  text "  remove-user\n"
  text "  execute-command\n"
  text "  list-workspaces\n"
  text "  workspace-ssh\n"
  text "  get-ssh-data\n"
  text "  graceful-stop\n"
  text "\n"
  text "Run '${CHE_IMAGE_FULLNAME} action' for action parameters"
  text "\n"
}

pre_cmd_action() {
  # Not loaded as part of the init process to save on download time
  load_utilities_images_if_not_done
}

post_cmd_action() {
  :
}

cmd_action() {
  docker_run $(get_docker_run_terminal_options) ${UTILITY_IMAGE_CHEACTION} "$@"
}
