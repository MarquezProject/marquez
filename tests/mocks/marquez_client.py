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
import uuid
from unittest.mock import create_autospec

from marquez_client import MarquezClient

log = logging.getLogger(__name__)


def get_mock_marquez_client():
    uuid_generator = generate_uuid()

    def create_job_run(*args, **kwargs):
        assert len(args) == 1
        log.debug("create_job_run()")
        return {"runId": str(next(uuid_generator))}

    def create_source(*args, **kwargs):
        assert len(args) >= 2
        name = args[0]
        # type = args[1] # commenting out as not used for now
        # connection_url = args[2]
        # source_type = connection_url.split(':')[1]
        return {'name': name}

    def create_dataset(*args, **kwargs):
        assert len(args) >= 2
        dataset_name = args[0]
        return {'name': dataset_name}

    client = create_autospec(MarquezClient)

    client.create_job_run.side_effect = create_job_run
    client.create_source.side_effect = create_source
    client.create_dataset.side_effect = create_dataset
    return client


def generate_uuid():
    idx = 0
    while True:
        yield uuid.UUID(int=idx)
        idx += 1
