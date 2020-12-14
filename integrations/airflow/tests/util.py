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
from unittest.mock import call

from dateutil.parser import parser
from tests.mocks.mock_id_mapping import MockIdMapping

log = logging.getLogger(__name__)


def execute_dag(dag, exec_date):
    dag._job_id_mapping = MockIdMapping()
    dagrun = dag.create_dagrun(
        run_id='airflow_run_id_0001',
        execution_date=parser().parse(exec_date),
        state='scheduled')
    dag.handle_callback(dagrun, success=True)


def assert_marquez_calls(client, namespace=None, owner=None,
                         datasources=None, datasets=None,
                         jobs=None, job_runs=None, run_ids=None):
    # check namespace was created
    assert_calls([call(namespace, owner)], client.create_namespace)

    # check datasource creation calls
    expected_calls = [call(ds, source_type, conn)
                      for ds, source_type, conn in datasources or []]
    assert_calls(expected_calls, client.create_source)

    # check dataset creation calls
    expected_calls = [call(name, ds_type, physical_name,
                           source_name, namespace_name=ns)
                      for name, ds_type, physical_name, source_name, ns
                      in datasets or []]
    assert_calls(expected_calls, client.create_dataset, any_order=True)

    # check job creation calls
    if jobs:
        assert_job_calls(jobs, client.create_job)

    # check job_run creation calls
    expected_calls = [call(name, start_ts, end_ts, run_args) for
                      name, start_ts, end_ts, run_args in job_runs or []]
    assert_calls(expected_calls, client.create_job_run)

    # check job_run marked as started and completed
    expected_calls = [call(run_id) for run_id in run_ids or []]
    assert_calls(expected_calls, client.mark_job_run_as_started)
    assert_calls(expected_calls, client.mark_job_run_as_completed)


def assert_calls(expected_calls, f, any_order=False):
    f.assert_has_calls(expected_calls, any_order=any_order)
    assert f.call_count == len(expected_calls)


def assert_job_calls(expected_jobs, jobs_client):
    assert jobs_client.call_count == len(expected_jobs)
    for i, c in enumerate(jobs_client.call_args_list):
        assert_job_call(expected_jobs[i], c)


def assert_job_call(expected, actual):
    log.debug("assert_job_call()")
    name, type, location, inputs, outputs, context, description = expected
    assert (name, type) == actual[0]
    assert location == actual[1].get('location')
    assert inputs == actual[1].get('input_dataset')
    assert outputs == actual[1].get('output_dataset')
    assert description == actual[1].get('description')

    if context and context.get('sql'):
        assert len(actual[1].get('context').get('sql')) > 1
    else:
        assert (not actual[1].get('context') or
                not actual[1].get('context').get('sql'))
