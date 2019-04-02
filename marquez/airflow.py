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
import pendulum
import airflow.models
from marquez_client.marquez import MarquezClient
from marquez.utils import JobIdMapping


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
        except Exception as e:
            logging.warning("[Marquez]\t{}".format(e))
        run = super(DAG, self).create_dagrun(*args, **kwargs)

        if marquez_jobrun_id:
            try:
                self._job_id_mapping.set(
                    JobIdMapping.make_key(run.dag_id, run.run_id),
                    marquez_jobrun_id)
            except Exception as e:
                logging.warning("[Marquez]\t{}".format(e))
        return run

    def handle_callback(self, *args, **kwargs):
        try:
            self.report_jobrun_change(args[0], **kwargs)
        except Exception as e:
            logging.warning("[Marquez]\t{}".format(e))
        return super().handle_callback(*args, **kwargs)

    def report_jobrun(self, run_args, execution_date):
        job_name = self.dag_id
        job_run_args = run_args
        start_time = DAG.to_airflow_time(execution_date)
        end_time = self.compute_endtime(execution_date)
        marquez_client = self.get_marquez_client()
        marquez_client.set_namespace(self.marquez_namespace)
        marquez_client.create_job(
            job_name, self.marquez_location,
            self.marquez_input_urns, self.marquez_output_urns,
            self.description)
        marquez_jobrun = marquez_client.create_job_run(
            job_name, job_run_args=job_run_args,
            nominal_start_time=start_time,
            nominal_end_time=end_time)

        marquez_jobrun_id = str(marquez_jobrun.run_id)

        marquez_client.mark_job_run_running(marquez_jobrun_id)
        self.log_marquez_event(['job_running',
                                marquez_jobrun_id,
                                start_time])
        return marquez_jobrun_id

    @staticmethod
    def to_airflow_time(execution_date):
        return pendulum.instance(execution_date).to_datetime_string()

    def compute_endtime(self, execution_date):
        end_time = self.following_schedule(execution_date)
        if end_time:
            end_time = DAG.to_airflow_time(end_time)
        return end_time

    def report_jobrun_change(self, dagrun, **kwargs):
        session = kwargs.get('session')
        marquez_job_run_id = self._job_id_mapping.pop(
            JobIdMapping.make_key(dagrun.dag_id, dagrun.run_id), session)
        if marquez_job_run_id:
            if kwargs.get('success'):
                self.get_marquez_client().mark_job_run_completed(
                    marquez_job_run_id)
            else:
                self.get_marquez_client().mark_job_run_failed(
                    marquez_job_run_id)
        state = 'COMPLETED' if kwargs.get('success') else 'FAILED'
        self.log_marquez_event(['job_state_change',
                                marquez_job_run_id,
                               state])

    def log_marquez_event(self, args):
        logging.info("\t".join(["[Marquez]",
                                self.marquez_namespace,
                                self.dag_id,
                                ] + args))

    def get_marquez_client(self):
        if not self._marquez_client:
            self._marquez_client = MarquezClient()
        return self._marquez_client
