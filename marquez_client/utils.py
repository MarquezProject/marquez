# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import json
import logging
import urllib
from http import HTTPStatus

from marquez_client.constants import NO_CONTENT_RESPONSE, NOT_FOUND


def handle_response(response_function):
    def wrapped_request(*args, **kwargs):
        response = response_function(*args, **kwargs)
        status_code = response.status_code
        response_text = response.text

        if status_code in [HTTPStatus.OK, HTTPStatus.CREATED]:
            if response_text:
                return json.loads(response_text)
            return
        if status_code in [HTTPStatus.NO_CONTENT]:
            return NO_CONTENT_RESPONSE
        exception_str = "{0} - {1}".format(status_code, response_text)
        logging.error(exception_str)
        if status_code in [HTTPStatus.NOT_FOUND]:
            return NOT_FOUND
        elif status_code in [
            HTTPStatus.BAD_REQUEST,
            HTTPStatus.UNPROCESSABLE_ENTITY
        ]:
            raise InvalidRequestError(exception_str)
        elif status_code in [
            HTTPStatus.SERVICE_UNAVAILABLE,
            HTTPStatus.INTERNAL_SERVER_ERROR,
        ]:
            raise APIError(exception_str)
        else:
            raise MarquezError()

    return wrapped_request


def _compose_path(path, path_args):
    if not path_args:
        return path
    encoded_path_args = list(
        map(lambda x: urllib.parse.quote(x, safe=''), path_args))
    return path.format(*encoded_path_args)


class MarquezError(Exception):
    """
    The generic error from the Marquez Service
    """
    pass


class APIError(MarquezError):
    """
    The error that is reported when the Marquez service does not handle
    a valid request properly.
    """
    pass


class InvalidRequestError(MarquezError):
    """
    The error that is reported when the user provides an invalid
    request to Marquez.
    """
    pass
