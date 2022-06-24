# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

import os

from marquez_client import MarquezClient
from marquez_client.constants import (
    DEFAULT_MARQUEZ_URL
)


# Marquez Clients
class Clients(object):
    @staticmethod
    def new_client():
        return MarquezClient(
            url=os.environ.get('MARQUEZ_URL', DEFAULT_MARQUEZ_URL),
            api_key=os.environ.get('MARQUEZ_API_KEY')
        )
