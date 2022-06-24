# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

import os

from marquez_client import Clients
from marquez_client.utils import Utils
from marquez_client.constants import DEFAULT_MARQUEZ_URL, API_PATH_V1

API_KEY = 'PuRx8GT3huSXlheDIRUK1YUatGpLVEuL'
API_BASE = f"{DEFAULT_MARQUEZ_URL}{API_PATH_V1}"


def test_new_client():
    os.environ['MARQUEZ_API_KEY'] = API_KEY

    from marquez_client.client import _USER_AGENT, _HEADERS
    headers_with_auth = {'User-Agent': _USER_AGENT}

    # Add API key to headers
    Utils.add_auth_to(headers_with_auth, API_KEY)

    client = Clients.new_client()
    assert client._api_base == API_BASE
    assert _HEADERS == headers_with_auth

    del os.environ['MARQUEZ_API_KEY']
