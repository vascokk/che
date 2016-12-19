#!/bin/bash
set -x

cd /home/antey/projects/che/exec-agent/target/linux_amd64
./che-websocket-terminal -static /home/antey/projects/che/plugins/plugin-terminal-ui/build  -addr :4000
