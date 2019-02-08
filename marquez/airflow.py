import json
import pendulum
import airflow.models
from airflow.utils.db import provide_session
from marquez_client.marquez import MarquezClient


class MarquezDag(airflow.models.DAG):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.mqz_client = MarquezClient()
        self.mqz_namespace = kwargs['default_args'].get('mqz_namespace', 'unknown')
        self.mqz_location = kwargs['default_args'].get('mqz_location', 'unknown')
        self.mqz_input_datasets = kwargs['default_args'].get('mqz_input_datasets', [])
        self.mqz_output_datasets = kwargs['default_args'].get('mqz_output_datasets', [])

    def create_dagrun(self, *args, **kwargs):
        job_name = self.dag_id
        job_run_args = "{}"                     # TODO retrieve from DAG/tasks
        start_time = pendulum.instance(kwargs['execution_date']).to_datetime_string()
        end_time = None

        self.mqz_client.set_namespace(self.mqz_namespace)
        self.mqz_client.create_job(job_name, self.mqz_location, self.mqz_input_datasets, self.mqz_output_datasets,
                                   self.description)
        mqz_job_run_id = self.mqz_client.create_job_run(job_name, job_run_args=job_run_args,
                                                        nominal_start_time=start_time,
                                                        nominal_end_time=end_time).run_id
        self.mqz_client.mark_job_run_running(mqz_job_run_id)

        self.marquez_log('job_running', json.dumps(
            {'namespace': self.mqz_namespace,
             'name': job_name,
             'description': self.description,
             'location': self.mqz_location,
             'runArgs': job_run_args,
             'nominal_start_time': start_time,
             'nominal_end_time': end_time,
             'jobrun_id': mqz_job_run_id,
             'inputDatasetUrns': self.mqz_input_datasets,
             'outputDatasetUrns': self.mqz_output_datasets
             }))

        run = super().create_dagrun(*args, **kwargs)
        airflow.models.Variable.set(run.run_id, mqz_job_run_id)

        return run

    def handle_callback(self, *args, **kwargs):
        job_name = self.dag_id
        mqz_job_run_id = self.get_and_delete(args[0].run_id)

        if mqz_job_run_id:

            if kwargs.get('success'):
                self.mqz_client.mark_job_run_completed(mqz_job_run_id)
                self.marquez_log('job_state_change',
                                 json.dumps({'job_name': job_name,
                                             'jobrun_id': mqz_job_run_id,
                                             'state': 'COMPLETED'}))
            else:
                self.mqz_client.mark_job_run_failed(mqz_job_run_id)
                self.marquez_log('job_state_change',
                                 json.dumps({'job_name': job_name,
                                             'jobrun_id': mqz_job_run_id,
                                             'state': 'FAILED'}))

        else:
            # TODO warn that the jobrun_id couldn't be found
            pass

        return super().handle_callback(*args, **kwargs)

    @provide_session
    def get_and_delete(self, key, session=None):
        q = session.query(airflow.models.Variable).filter(airflow.models.Variable.key == key)
        if q.first() is None:
            return
        else:
            val = q.first().val
            q.delete(synchronize_session=False)
            return val

    @provide_session
    def marquez_log(self, event, extras, session=None):
        session.add(airflow.models.Log(
            event=event,
            task_instance=None,
            owner="marquez",
            extra=extras,
            task_id=None,
            dag_id=self.dag_id))
