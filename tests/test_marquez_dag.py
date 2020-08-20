import sys
from unittest.mock import patch

import pytest
from tests.mocks.git_mock import execute_git_mock
from tests.mocks.marquez_client import get_mock_marquez_client
from tests.util import assert_marquez_calls, execute_dag

import airflow.jobs  # noqa: F401


@pytest.mark.skip(reason="need to revisit how DAGs are tested")
@patch('marquez_airflow.dag.MarquezClient',
       return_value=get_mock_marquez_client())
@patch('marquez_airflow.utils.execute_git',
       side_effect=execute_git_mock)
def test_dummy_dag(git_mock, client_mock, dagbag):
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
