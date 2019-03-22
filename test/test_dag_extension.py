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

from airflow.utils.state import State
from contextlib import contextmanager
from datetime import datetime
from marquez.airflow import DAG
from marquez_client.marquez import MarquezClient
from unittest.mock import Mock, create_autospec, patch

import airflow.models
import marquez.utils
import os
import pytest


class MockDag:
    def __init__(self, dag_id, schedule_interval=None, location=None,
                 input_urns=None, output_urns=None,
                 start_date=None, description=None,
                 marquez_run_id=None, airflow_run_id=None,
                 mock_marquez_client=True):
        self.dag_id = dag_id
        self.schedule_interval = schedule_interval or '*/10 * * * *'
        self.location = location or 'test_location'
        self.input_urns = input_urns or []
        self.output_urns = output_urns or []
        self.start_date = start_date or datetime(2019, 1, 31, 0, 0, 0)
        self.description = description or 'test description'

        self.marquez_run_id = marquez_run_id or '71d29487-0b54-4ae1-9295'
        self.airflow_run_id = airflow_run_id or 'airflow_run_id_123456'

        self.marquez_dag = DAG(
            self.dag_id,
            schedule_interval=self.schedule_interval,
            default_args={'marquez_location': self.location,
                          'marquez_input_urns': self.input_urns,
                          'marquez_output_urns': self.output_urns,
                          'owner': 'na',
                          'depends_on_past': False,
                          'start_date': self.start_date},
            description=self.description)
        if mock_marquez_client:
            self.marquez_dag._marquez_client = \
                make_mock_marquez_client(self.marquez_run_id)


@contextmanager
def execute_test(test_dag, mock_dag_run, mock_set):
    mock_dag_run.return_value = make_mock_airflow_jobrun(
        test_dag.dag_id,
        test_dag.airflow_run_id)
    yield
    # Test the corresponding marquez calls
    assert_marquez_calls_for_dagrun(test_dag)

    # Assert there is a job_id mapping being created
    mock_set.assert_called_once_with(
        marquez.utils.JobIdMapping.make_key(test_dag.dag_id,
                                            test_dag.airflow_run_id),
        test_dag.marquez_run_id)


@patch.object(airflow.models.DAG, 'create_dagrun')
@patch.object(marquez.utils.JobIdMapping, 'set')
def test_create_dagrun(mock_set, mock_dag_run):

    test_dag = MockDag('test_dag_id')
    with execute_test(test_dag, mock_dag_run, mock_set):
        test_dag.marquez_dag.create_dagrun(state=State.RUNNING,
                                           run_id=test_dag.airflow_run_id,
                                           execution_date=test_dag.start_date)


@patch.object(airflow.models.DAG, 'create_dagrun')
@patch.object(marquez.utils.JobIdMapping, 'set')
def test_dag_once_schedule(mock_set, mock_dag_run):
    test_dag = MockDag('test_dag_id', schedule_interval="@once")

    with execute_test(test_dag, mock_dag_run, mock_set):
        test_dag.marquez_dag.create_dagrun(state=State.RUNNING,
                                           run_id=test_dag.airflow_run_id,
                                           execution_date=test_dag.start_date)


@patch.object(airflow.models.DAG, 'create_dagrun')
@patch.object(marquez.utils.JobIdMapping, 'set')
def test_no_marquez_connection(mock_set, mock_dag_run):
    test_dag = MockDag('test_dag_id', mock_marquez_client=False)

    mock_dag_run.return_value = make_mock_airflow_jobrun(
            test_dag.dag_id,
            test_dag.airflow_run_id)

    test_dag.marquez_dag.create_dagrun(state=State.RUNNING,
                                       run_id=test_dag.airflow_run_id,
                                       execution_date=test_dag.start_date)

    mock_set.assert_not_called()


def test_custom_namespace():
    os.environ['MARQUEZ_NAMESPACE'] = 'test_namespace'
    test_dag = MockDag('test_dag_id')
    assert test_dag.marquez_dag.marquez_namespace == 'test_namespace'


def test_default_namespace():
    os.environ.clear()
    test_dag = MockDag('test_dag_id')
    assert test_dag.marquez_dag.marquez_namespace == \
        DAG.DEFAULT_NAMESPACE


def assert_marquez_calls_for_dagrun(test_dag):
    marquez_client = test_dag.marquez_dag._marquez_client

    marquez_client.set_namespace.assert_called_with(
        test_dag.marquez_dag.marquez_namespace)

    marquez_client.create_job.assert_called_once_with(
        test_dag.dag_id, test_dag.location, test_dag.input_urns,
        test_dag.output_urns, test_dag.description)

    marquez_client.create_job_run.assert_called_once_with(
        test_dag.dag_id, "{}",
        DAG.to_airflow_time(test_dag.start_date),
        test_dag.marquez_dag.compute_endtime(test_dag.start_date))


def make_mock_marquez_client(run_id):
    mock_marquez_jobrun = Mock()
    mock_marquez_jobrun.run_id = run_id
    mock_marquez_client = create_autospec(MarquezClient)
    mock_marquez_client.create_job_run.return_value = mock_marquez_jobrun
    return mock_marquez_client


def make_mock_airflow_jobrun(dag_id, airflow_run_id):
    mock_airflow_jobrun = Mock()
    mock_airflow_jobrun.run_id = airflow_run_id
    mock_airflow_jobrun.dag_id = dag_id
    return mock_airflow_jobrun


if __name__ == "__main__":
    pytest.main()
