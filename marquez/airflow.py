import json
import pendulum
from airflow.models import DAG, Log
from airflow.utils.db import provide_session
from marquez_client.marquez import MarquezClient

from marquez.utils import JobIdMapping


class MarquezDag(DAG):
    _job_id_mapping = None
    _mqz_client = None

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.mqz_namespace = kwargs['default_args'].get('mqz_namespace', 'unknown')
        self.mqz_location = kwargs['default_args'].get('mqz_location', 'unknown')
        self.mqz_input_datasets = kwargs['default_args'].get('mqz_input_datasets', [])
        self.mqz_output_datasets = kwargs['default_args'].get('mqz_output_datasets', [])
        self._job_id_mapping = JobIdMapping()

    def create_dagrun(self, *args, **kwargs):
        run_args = "{}"  # TODO extract the run Args from the tasks
        mqz_job_run_id = self.report_jobrun(run_args, kwargs['execution_date'])
        run = super(MarquezDag, self).create_dagrun(*args, **kwargs)
        self._job_id_mapping.set(JobIdMapping.make_key(run.dag_id, run.run_id), mqz_job_run_id)
        return run

    def handle_callback(self, *args, **kwargs):
        self.report_jobrun_change(args[0], **kwargs)
        return super().handle_callback(*args, **kwargs)

    def report_jobrun(self, run_args, execution_date):
        job_name = self.dag_id
        job_run_args = run_args
        start_time = pendulum.instance(execution_date).to_datetime_string()
        end_time = pendulum.instance(self.following_schedule(execution_date)).to_datetime_string()
        mqz_client = self.get_mqz_client()
        mqz_client.set_namespace(self.mqz_namespace)
        mqz_client.create_job(job_name, self.mqz_location, self.mqz_input_datasets,
                              self.mqz_output_datasets, self.description)
        mqz_job_run_id = str(mqz_client.create_job_run(
            job_name, job_run_args=job_run_args, nominal_start_time=start_time, nominal_end_time=end_time).run_id)
        mqz_client.mark_job_run_running(mqz_job_run_id)

        self.log_marquez_event('job_running',
                               namespace=self.mqz_namespace,
                               name=job_name,
                               description=self.description,
                               location=self.mqz_location,
                               runArgs=job_run_args,
                               nominal_start_time=start_time,
                               nominal_end_time=end_time,
                               jobrun_id=mqz_job_run_id,
                               inputDatasetUrns=self.mqz_input_datasets,
                               outputDatasetUrns=self.mqz_output_datasets)
        return mqz_job_run_id

    def report_jobrun_change(self, dagrun, **kwargs):
        mqz_job_run_id = self._job_id_mapping.pop(JobIdMapping.make_key(dagrun.dag_id, dagrun.run_id))
        if mqz_job_run_id:
            if kwargs.get('success'):
                self.get_mqz_client().mark_job_run_completed(mqz_job_run_id)
            else:
                self.get_mqz_client().mark_job_run_failed(mqz_job_run_id)
        self.log_marquez_event('job_state_change' if mqz_job_run_id else 'job_state_change_LOST',
                               job_name=self.dag_id,
                               jobrun_id=mqz_job_run_id,
                               state='COMPLETED' if kwargs.get('success') else 'FAILED',
                               reason=kwargs['reason'])

    @provide_session
    def log_marquez_event(self, event, session=None, **kwargs):
        session.add(Log(
            event=event,
            task_instance=None,
            owner="marquez",
            extra=json.dumps(kwargs),
            task_id=None,
            dag_id=self.dag_id))

    def get_mqz_client(self):
        if not self._mqz_client:
            self._mqz_client = MarquezClient()
        return self._mqz_client
