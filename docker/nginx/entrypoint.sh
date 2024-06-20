#!/bin/bash
#
# Copyright 2018-2024 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./entrypoint.sh

envsubst '$MARQUEZ_HOST,$MARQUEZ_PORT' < /etc/nginx/nginx.template > /etc/nginx/nginx.conf

# Start Nginx
nginx -g 'daemon off;'
