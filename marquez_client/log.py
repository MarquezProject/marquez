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

import logging
import os

_MARQUEZ_LOG_LEVEL = os.environ.get('MARQUEZ_LOG_LEVEL')

_LOG_FORMAT = '%(asctime)s | %(name)s | %(levelname)s | %(message)s'
_LOG_LEVELS = {
    'DEBUG': logging.DEBUG,
    'INFO': logging.INFO,
    'WARN': logging.WARN,
    'ERROR': logging.ERROR
}


def _log_level():
    if _MARQUEZ_LOG_LEVEL in _LOG_LEVELS:
        return _LOG_LEVELS[_MARQUEZ_LOG_LEVEL]
    else:
        return _LOG_LEVELS['ERROR']


_CONSOLE = logging.StreamHandler()
_CONSOLE.setLevel(_log_level())
_CONSOLE.setFormatter(logging.Formatter(_LOG_FORMAT))

_LOG = logging.getLogger('marquez_client')
_LOG.setLevel(_log_level())
_LOG.addHandler(_CONSOLE)


def debug(msg, **extra):
    _LOG.debug(_fmt(msg, **extra))


def info(msg, **extra):
    _LOG.info(_fmt(msg, **extra))


def warn(msg, **extra):
    _LOG.warn(_fmt(msg, **extra))


def error(msg, **extra):
    _LOG.error(_fmt(msg, **extra))


def _fmt(msg, **extra):
    def fmt(k, v):
        return f'{k}={v}'
    msg_with_extra = [msg]
    msg_with_extra.extend(fmt(k, v) for k, v in sorted(extra.items()))
    return u' '.join(msg_with_extra)
