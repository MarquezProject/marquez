from datetime import datetime
from unittest.mock import Mock, create_autospec, patch

import pytest

import airflow.models
import marquez.utils
import pendulum
from airflow.utils.state import State
from croniter import croniter
from marquez.airflow import MarquezDag
from marquez_client.marquez import MarquezClient


class Context:
    location = 'github://test_dag_location'
    dag_id = 'test-dag-1'
    namespace = 'test-namespace-1'
    data_inputs = ["s3://data_input_1", "s3://data_input_2"]
    data_outputs = ["s3://some_output_data"]
    owner = 'test_owner'
    description = 'this is a test DAG'
    airflow_run_id = 'airflow_run_id_123456'
    mqz_run_id = '71d29487-0b54-4ae1-9295-efd87f190c57'
    start_date = datetime(2019, 1, 31, 0, 0, 0)
    execution_date = datetime(2019, 2, 2, 0, 0, 0)
    schedule_interval = '*/10 * * * *'

    dag = None

    def __init__(self):
        self.dag = MarquezDag(
            self.dag_id,
            schedule_interval=self.schedule_interval,
            default_args={'mqz_namespace': self.namespace,
                          'mqz_location': self.location,
                          'mqz_input_datasets': self.data_inputs,
                          'mqz_output_datasets': self.data_outputs,
                          'owner': self.owner,
                          'depends_on_past': False,
                          'start_date': self.start_date},
            description=self.description)


@pytest.fixture(scope="module")
def context():
    return Context()


@patch.object(airflow.models.DAG, 'create_dagrun')
@patch.object(marquez.utils.JobIdMapping, 'set')
def test_create_dagrun(mock_set, mock_dag_run, context):

    dag = context.dag
    mock_mqz_client = make_mock_mqz_client(context.mqz_run_id)
    dag._mqz_client = mock_mqz_client  # Use a mock marquez-python client
    mock_dag_run.return_value = make_mock_airflow_jobrun(dag.dag_id, context.airflow_run_id)

    # trigger an airflow DagRun
    dag.create_dagrun(state=State.RUNNING, run_id=context.airflow_run_id, execution_date=context.execution_date)

    # check Marquez client was called with expected arguments
    mock_mqz_client.set_namespace.assert_called_with(context.namespace)
    mock_mqz_client.create_job.assert_called_once_with(context.dag_id, context.location, context.data_inputs,
                                                       context.data_outputs, context.description)
    mock_mqz_client.create_job_run.assert_called_once_with(
        context.dag_id,
        "{}",
        to_airflow_datetime_str(context.execution_date),
        to_airflow_datetime_str(compute_end_time(context.schedule_interval, context.execution_date)))

    # Test if airflow's create_dagrun() is called with the expected arguments
    mock_dag_run.assert_called_once_with(state=State.RUNNING,
                                         run_id=context.airflow_run_id,
                                         execution_date=context.execution_date)

    # Assert there is a job_id mapping being created
    mock_set.assert_called_once_with(marquez.utils.JobIdMapping.make_key(context.dag_id, context.airflow_run_id),
                                     context.mqz_run_id)


def make_mock_mqz_client(run_id):
    mock_mqz_run = Mock()
    mock_mqz_run.run_id = run_id
    mock_mqz_client = create_autospec(MarquezClient)
    mock_mqz_client.create_job_run.return_value = mock_mqz_run
    return mock_mqz_client


def make_mock_airflow_jobrun(dag_id, airflow_run_id):
    mock_airflow_jobrun = Mock()
    mock_airflow_jobrun.run_id = airflow_run_id
    mock_airflow_jobrun.dag_id = dag_id
    return mock_airflow_jobrun


def compute_end_time(schedule_interval, start_time):
    return datetime.utcfromtimestamp(croniter(schedule_interval, start_time).get_next())


def to_airflow_datetime_str(dt):
    return pendulum.instance(dt).to_datetime_string()


if __name__ == "__main__":
    pytest.main()
