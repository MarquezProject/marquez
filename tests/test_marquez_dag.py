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
import sys
from unittest.mock import patch

import pytest
from tests.mocks.git_mock import execute_git_mock
from tests.mocks.marquez_client import get_mock_marquez_client
from tests.util import assert_marquez_calls, execute_dag

import airflow.jobs  # noqa: F401

log = logging.getLogger(__name__)


@pytest.mark.skip(reason="need to revisit how DAGs are tested")
@patch('marquez_airflow.dag.MarquezClient',
       return_value=get_mock_marquez_client())
@patch('marquez_airflow.utils.execute_git',
       side_effect=execute_git_mock)
def test_dummy_dag(git_mock, client_mock, dagbag):
    log.debug("test_dummy_dag()")
    execute_dag(dagbag.dags['test_dummy_dag'], '2020-01-08T01:00:00Z')
    assert_marquez_calls(
        client_mock(), namespace='test-marquez', owner='default_owner',
        jobs=[(
            'test_dummy_dag.test_dummy',
            'BATCH',
            'https://github.com/MarquezProject/marquez-airflow/blob/abcd1234/'
            'tests/test_dags/test_dummy_dag.py', [], [],
            {'sql': False},
            'Test dummy DAG')],
        job_runs=[('test_dummy_dag.test_dummy',
                   '2020-01-08T01:00:00Z', '2020-01-08T01:02:00Z',
                   {"external_trigger": False})],
        run_ids=['00000000-0000-0000-0000-000000000000'])


if __name__ == "__main__":
    pytest.main([sys.argv[0]])
