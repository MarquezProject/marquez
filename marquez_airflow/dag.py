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

import os
import airflow.models
import time

from marquez_airflow import log
from marquez_airflow.utils import JobIdMapping
from marquez_client import MarquezClient


class DAG(airflow.models.DAG):
    DEFAULT_NAMESPACE = 'default'
    _job_id_mapping = None
    _marquez_client = None

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.marquez_namespace = os.environ.get('MARQUEZ_NAMESPACE') or \
            DAG.DEFAULT_NAMESPACE
        self.marquez_location = kwargs['default_args'].get(
            'marquez_location', 'unknown')
        self.marquez_input_urns = kwargs['default_args'].get(
            'marquez_input_urns', [])
        self.marquez_output_urns = kwargs['default_args'].get(
            'marquez_output_urns', [])
        self._job_id_mapping = JobIdMapping()

    def create_dagrun(self, *args, **kwargs):
        run_args = "{}"  # TODO extract the run Args from the tasks
        marquez_jobrun_id = None
        try:
            marquez_jobrun_id = self.report_jobrun(run_args,
                                                   kwargs['execution_date'])
            log.info(f'Successfully recorded job run.',
                     airflow_dag_id=self.dag_id,
                     marquez_run_id=marquez_jobrun_id,
                     marquez_namespace=self.marquez_namespace)
        except Exception as e:
            log.error(f'Failed to record job run: {e}',
                      airflow_dag_id=self.dag_id,
                      marquez_namespace=self.marquez_namespace)
            pass

        run = super(DAG, self).create_dagrun(*args, **kwargs)

        if marquez_jobrun_id:
            try:
                self._job_id_mapping.set(
                    JobIdMapping.make_key(run.dag_id, run.run_id),
                    marquez_jobrun_id)
            except Exception as e:
                log.error(f'Failed job run lookup: {e}',
                          airflow_dag_id=self.dag_id,
                          airflow_run_id=run.run_id,
                          marquez_run_id=marquez_jobrun_id,
                          marquez_namespace=self.marquez_namespace)
                pass

        return run

    def handle_callback(self, *args, **kwargs):
        try:
            self.report_jobrun_change(args[0], **kwargs)
        except Exception as e:
            log.error(
                f'Failed to record job run state change: {e}',
                dag_id=self.dag_id)

        return super().handle_callback(*args, **kwargs)

    def report_jobrun(self, run_args, execution_date):
        now_ms = self._now_ms()

        job_name = self.dag_id
        start_time = execution_date.format("%Y-%m-%dT%H:%M:%SZ")
        end_time = self.compute_endtime(execution_date)
        if end_time:
            end_time = end_time.strftime("%Y-%m-%dT%H:%M:%SZ")
        marquez_client = self.get_marquez_client()

        marquez_client.create_job(
            job_name, self.marquez_location,
            self.marquez_input_urns, self.marquez_output_urns,
            description=self.description)
        log.info(f'Successfully recorded job: {job_name}',
                 airflow_dag_id=self.dag_id,
                 marquez_namespace=self.marquez_namespace)

        marquez_jobrun = marquez_client.create_job_run(
            job_name, run_args=run_args,
            nominal_start_time=start_time,
            nominal_end_time=end_time)

        marquez_jobrun_id = marquez_jobrun.get('runId')
        if marquez_jobrun_id:
            marquez_client.mark_job_run_as_running(marquez_jobrun_id)
            log.info(f'Successfully recorded job run: {job_name}',
                     airflow_dag_id=self.dag_id,
                     airflow_dag_execution_time=start_time,
                     marquez_run_id=marquez_jobrun_id,
                     marquez_namespace=self.marquez_namespace,
                     duration_ms=(self._now_ms() - now_ms))
        else:
            log.warn(f'Run id found not found: {job_name}',
                     airflow_dag_id=self.dag_id,
                     airflow_dag_execution_time=start_time,
                     marquez_run_id=marquez_jobrun_id,
                     marquez_namespace=self.marquez_namespace,
                     duration_ms=(self._now_ms() - now_ms))

        return marquez_jobrun_id

    def compute_endtime(self, execution_date):
        return self.following_schedule(execution_date)

    def report_jobrun_change(self, dagrun, **kwargs):
        session = kwargs.get('session')
        marquez_job_run_id = self._job_id_mapping.pop(
            JobIdMapping.make_key(dagrun.dag_id, dagrun.run_id), session)
        if marquez_job_run_id:
            log.info(f'Found job run.',
                     airflow_dag_id=dagrun.dag_id,
                     airflow_run_id=dagrun.run_id,
                     marquez_run_id=marquez_job_run_id,
                     marquez_namespace=self.marquez_namespace)

            if kwargs.get('success'):
                self.get_marquez_client().mark_job_run_as_completed(
                    marquez_job_run_id)
            else:
                self.get_marquez_client().mark_job_run_as_failed(
                    marquez_job_run_id)

        state = 'COMPLETED' if kwargs.get('success') else 'FAILED'
        log.info(f'Marked job run as {state}.',
                 airflow_dag_id=dagrun.dag_id,
                 airflow_run_id=dagrun.run_id,
                 marquez_run_id=marquez_job_run_id,
                 marquez_namespace=self.marquez_namespace)

    def get_marquez_client(self):
        if not self._marquez_client:
            self._marquez_client = MarquezClient(
                namespace_name=self.marquez_namespace)
            self._marquez_client.create_namespace(self.marquez_namespace,
                                                  "default_owner")
        return self._marquez_client

    @staticmethod
    def _now_ms():
        return int(round(time.time() * 1000))
