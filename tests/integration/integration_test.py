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
import subprocess
import sys

import pytest
import requests
from marquez_client import MarquezClient
from urllib3.util.retry import Retry


def test_data_in_marquez(wait_for_marquez, init_airflow_db):
    dag_id = "test_dag"
    execution_date = "2019-02-01T00:00:00"
    namespace = "integration_test"

    c = MarquezClient(namespace_name=namespace)

    assert(trigger_dag(dag_id, execution_date))
    assert(check_dag_state(dag_id, execution_date))
    result = c.get_namespace(namespace)
    assert(result and result['name'] == namespace)

    expected_jobs = ["test_dag.run_this_1", "test_dag.run_this_2"]
    for expected_job in expected_jobs:
        result = c.get_job(expected_job)
        assert(result and result['name'] == expected_job)


def trigger_dag(dag_id, execution_date):
    process = airflow_cli(['backfill', dag_id, '-s', execution_date])
    return not process.returncode


def check_dag_state(dag_id, execution_date):
    process = airflow_cli(['dag_state', dag_id, execution_date])
    dag_state = process.stdout.decode('utf8').strip().split('\n')[-1]
    return not process.returncode and dag_state == 'success'


def airflow_cli(args):
    cmd = ['airflow'] + args
    process = subprocess.run(cmd,
                             stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE)
    if process.stderr:
        logging.error(process.stderr)
    return process


@pytest.fixture(scope="module")
def init_airflow_db():
    process = airflow_cli(['initdb'])
    return not process.returncode


@pytest.fixture(scope="module")
def wait_for_marquez():
    url = 'http://{}:{}/ping'.format(os.environ['MARQUEZ_HOST'],
                                     os.environ['MARQUEZ_ADMIN_PORT'])
    session = requests.Session()
    retry = Retry(total=5, backoff_factor=0.5)
    adapter = requests.adapters.HTTPAdapter(max_retries=retry)
    session.mount('http://', adapter)
    session.get(url)


if __name__ == "__main__":
    pytest.main([sys.argv[0]])
