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

import pytest
import mock

from airflow.models import (TaskInstance, DagRun)
from airflow.operators.dummy_operator import DummyOperator
from airflow.utils.db import provide_session
from airflow.utils.dates import days_ago
from airflow.utils import timezone
from airflow.utils.state import State

from marquez_client.models import JobType

from marquez_airflow.extractors import BaseExtractor, StepMetadata
from marquez_airflow import DAG
from marquez_airflow.utils import get_location, get_job_name

NO_INPUTS = []
NO_OUTPUTS = []

DEFAULT_DATE = timezone.datetime(2016, 1, 1)

DAG_ID = 'test_dag'
DAG_RUN_ID = 'test_run_id_for_task_completed_and_failed'
DAG_RUN_ARGS = {'external_trigger': False}
DAG_NAMESPACE = 'default'
DAG_OWNER = 'anonymous'
DAG_DESCRIPTION = \
    'A simple DAG to test the marquez.DAG metadata extraction flow.'

DAG_DEFAULT_ARGS = {
    'owner': DAG_OWNER,
    'depends_on_past': False,
    'start_date': days_ago(1),
    'email_on_failure': False,
    'email_on_retry': False,
    'email': ['owner@test.com']
}

DAG = DAG(
    DAG_ID,
    schedule_interval='@daily',
    default_args=DAG_DEFAULT_ARGS,
    description=DAG_DESCRIPTION
)

TASK_ID_COMPLETED = 'test_task_completed'
TASK_ID_FAILED = 'test_task_failed'


@pytest.fixture
@provide_session
def clear_db_airflow_dags(session=None):
    session.query(DagRun).delete()
    session.query(TaskInstance).delete()


class DummyExtractor(BaseExtractor):
    operator_class = DummyOperator

    def __init__(self, operator):
        super().__init__(operator)

    def extract(self) -> [StepMetadata]:
        return [StepMetadata(
            name=get_job_name(task=self.operator),
            context={
                "extract": "extract"
            }
        )]

    def extract_on_complete(self, task_instance) -> [StepMetadata]:
        return [StepMetadata(
            name=get_job_name(task=self.operator),
            context={
                "extract_on_complete": "extract_on_complete"
            }
        )]


@mock.patch('marquez_airflow.DAG.get_or_create_marquez_client')
@provide_session
def test_marquez_dag(mock_get_or_create_marquez_client,
                     clear_db_airflow_dags, session=None):
    # (1) Mock the marquez client method calls
    mock_marquez_client = mock.Mock()
    mock_get_or_create_marquez_client.return_value = mock_marquez_client

    # (2) Add task that will be marked as completed
    task_will_complete = DummyOperator(
        task_id=TASK_ID_COMPLETED,
        dag=DAG
    )
    completed_task_location = get_location(task_will_complete.dag.fileloc)

    # (3) Add task that will be marked as failed
    task_will_fail = DummyOperator(
        task_id=TASK_ID_FAILED,
        dag=DAG
    )
    failed_task_location = get_location(task_will_complete.dag.fileloc)

    DAG._extractors[task_will_complete.__class__] = DummyExtractor

    # (4) Create DAG run and mark as running
    dagrun = DAG.create_dagrun(
        run_id=DAG_RUN_ID,
        execution_date=DEFAULT_DATE,
        state=State.RUNNING)

    # Assert namespace meta call
    mock_marquez_client.create_namespace.assert_called_once_with(DAG_NAMESPACE,
                                                                 DAG_OWNER)

    # Assert source and dataset meta calls
    mock_marquez_client.create_datasource.assert_not_called()
    mock_marquez_client.create_dataset.assert_not_called()

    # Assert job meta calls
    create_job_calls = [
        mock.call(
            job_name=f"{DAG_ID}.{TASK_ID_COMPLETED}",
            job_type=JobType.BATCH,
            location=completed_task_location,
            input_dataset=NO_INPUTS,
            output_dataset=NO_OUTPUTS,
            context=mock.ANY,
            description=DAG_DESCRIPTION,
            namespace_name=DAG_NAMESPACE
        ),
        mock.call(
            job_name=f"{DAG_ID}.{TASK_ID_FAILED}",
            job_type=JobType.BATCH,
            location=failed_task_location,
            input_dataset=NO_INPUTS,
            output_dataset=NO_OUTPUTS,
            context=mock.ANY,
            description=DAG_DESCRIPTION,
            namespace_name=DAG_NAMESPACE
        )
    ]
    mock_marquez_client.create_job.assert_has_calls(create_job_calls)

    # Assert job run meta calls
    create_job_run_calls = [
        mock.call(
            job_name=f"{DAG_ID}.{TASK_ID_COMPLETED}",
            run_id=mock.ANY,
            run_args=DAG_RUN_ARGS,
            nominal_start_time=mock.ANY,
            nominal_end_time=mock.ANY,
            namespace_name=DAG_NAMESPACE
        ),
        mock.call(
            job_name=f"{DAG_ID}.{TASK_ID_FAILED}",
            run_id=mock.ANY,
            run_args=DAG_RUN_ARGS,
            nominal_start_time=mock.ANY,
            nominal_end_time=mock.ANY,
            namespace_name=DAG_NAMESPACE
        )
    ]
    mock_marquez_client.create_job_run.assert_has_calls(create_job_run_calls)

    # Assert start run meta calls
    start_job_run_calls = [
        mock.call(run_id=mock.ANY),
        mock.call(run_id=mock.ANY)
    ]
    mock_marquez_client.mark_job_run_as_started.assert_has_calls(
        start_job_run_calls
    )

    # (5) Start task that will be marked as completed
    task_will_complete.run(start_date=DEFAULT_DATE, end_date=DEFAULT_DATE)

    DAG.handle_callback(dagrun, success=True, session=session)
    mock_marquez_client.mark_job_run_as_completed.assert_called_once_with(
        run_id=mock.ANY
    )

    # When a task run completes, the task outputs are also updated in order
    # to link a job version (=task version) to a dataset version.
    # Using a DummyOperator, no outputs exists, so assert that the create
    # dataset call is not invoked.
    mock_marquez_client.create_dataset.assert_not_called()

    # (6) Start task that will be marked as failed
    task_will_fail.run(start_date=DEFAULT_DATE, end_date=DEFAULT_DATE)

    DAG.handle_callback(dagrun, success=False, session=session)
    mock_marquez_client.mark_job_run_as_failed.assert_called_once_with(
        run_id=mock.ANY
    )

    # Assert an attempt to version the outputs of a task is not made when
    # a task fails
    mock_marquez_client.create_dataset.assert_not_called()
