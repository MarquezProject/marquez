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

import json
import os
import time
from uuid import uuid4

from airflow import LoggingMixin
from airflow.contrib.operators.bigquery_operator import BigQueryOperator
from pendulum import Pendulum

import airflow.models
from airflow.operators.postgres_operator import PostgresOperator

from marquez_airflow.extractors.bigquery_extractor import BigQueryExtractor
from marquez_airflow.utils import (
    JobIdMapping,
    get_location,
    add_airflow_info_to
)
from marquez_airflow.extractors import (Dataset, Source, StepMetadata)
from marquez_airflow.extractors.postgres_extractor import PostgresExtractor

from marquez_client import Clients
from marquez_client.models import JobType

_NOMINAL_TIME_FORMAT = "%Y-%m-%dT%H:%M:%SZ"

_DAG_DEFAULT_NAMESPACE = 'default'
_DAG_DEFAULT_OWNER = 'anonymous'


class DAG(airflow.models.DAG, LoggingMixin):
    DEFAULT_NAMESPACE = 'default'
    _job_id_mapping = None
    _marquez_client = None

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._marquez_dataset_cache = {}
        self._marquez_source_cache = {}
        self.marquez_namespace = os.getenv('MARQUEZ_NAMESPACE',
                                           _DAG_DEFAULT_NAMESPACE)
        self._job_id_mapping = JobIdMapping()
        # TODO: Manually define operator->extractor mappings for now,
        # but we'll want to encapsulate this logic in an 'Extractors' class
        # with more convenient methods (ex: 'Extractors.extractor_for_task()')
        self._extractors = {
            PostgresOperator: PostgresExtractor,
            BigQueryOperator: BigQueryExtractor
            # Append new extractors here
        }
        self.log.debug(
            f"DAG successfully created with extractors: {self._extractors}"
        )

    def create_dagrun(self, *args, **kwargs):
        # run Airflow's create_dagrun() first
        dagrun = super(DAG, self).create_dagrun(*args, **kwargs)

        create_dag_start_ms = self._now_ms()
        execution_date = kwargs.get('execution_date')
        run_args = {
            'external_trigger': kwargs.get('external_trigger', False)
        }

        # Marquez metadata collection
        try:
            # TODO: Use 'anonymous' owner for now, but we may want to use
            # the 'owner' attribute defined via default_args for a DAG
            self.log.debug(
                f"Creating namespace '{self.marquez_namespace}' with "
                f"owner '{_DAG_DEFAULT_OWNER}'...")
            self.get_or_create_marquez_client()\
                .create_namespace(self.marquez_namespace, _DAG_DEFAULT_OWNER)

            # Register each task in the DAG
            for task_id, task in self.task_dict.items():
                t = self._now_ms()
                try:
                    self.report_task(
                        dagrun.run_id,
                        execution_date,
                        task,
                        self._extractors.get(task.__class__),
                        run_args=run_args)
                except Exception as e:
                    self.log.error(
                        f'Failed to record task: {e} '
                        f'airflow_dag_id={self.dag_id} '
                        f'task_id={task_id} '
                        f'marquez_namespace={self.marquez_namespace} '
                        f'duration_ms={(self._now_ms() - t)}')

            self.log.info(
                f'Successfully recorded metadata: '
                f'airflow_dag_id={self.dag_id} '
                f'marquez_namespace={self.marquez_namespace} '
                f'duration_ms={(self._now_ms() - create_dag_start_ms)}')

        except Exception as e:
            self.log.error(
                f'Failed to record metadata: {e} '
                f'airflow_dag_id={self.dag_id} '
                f'marquez_namespace={self.marquez_namespace} '
                f'duration_ms={(self._now_ms() - create_dag_start_ms)}')

        return dagrun

    def handle_callback(self, *args, **kwargs):
        try:
            dagrun = args[0]
            task_instances = dagrun.get_task_instances()
            for ti in task_instances:
                extractor = self._extractors.get(ti.operator.__class__)
                if extractor:
                    self.log.debug(
                        f"Using extractor '{extractor}' to apply partial "
                        f"metadata updates for operator '{ti.operator}' "
                        f"in DAG '{self.dag_id}'."
                    )

                    steps_meta = add_airflow_info_to(
                        ti.operator,
                        extractor(ti.operator).extract_on_complete(ti)
                    )
                    self.log.debug(f'steps_meta: {steps_meta}')

                    # FIXME: replace with update api
                    for step in steps_meta:
                        self.get_marquez_client().create_job(
                            job_name=step.name,
                            job_type=JobType.BATCH,
                            location=(step.location or self._get_location(ti)),
                            input_dataset=step.inputs,
                            output_dataset=step.outputs,
                            context=step.context,
                            description=self.description,
                            namespace_name=self.marquez_namespace)

                try:
                    self.report_jobrun_change(
                        ti, dagrun.run_id, **kwargs)
                except Exception as e:
                    self.log.error(
                        f'Failed to record task run state change: {e} '
                        f'dag_id={self.dag_id}')

        except Exception as e:
            self.log.error(
                f'Failed to record dagrun state change: {e} '
                f'dag_id={self.dag_id}')

        return super().handle_callback(*args, **kwargs)

    def report_task(self,
                    dag_run_id,
                    execution_date,
                    task,
                    extractor,
                    marquez_job_run_id=None,
                    run_args=None):
        self.log.info("report_task()")
        report_job_start_ms = self._now_ms()
        marquez_client = self.get_or_create_marquez_client()
        if execution_date:
            start_time = self._to_iso_8601(execution_date)
            end_time = self.compute_endtime(execution_date)
        else:
            start_time = None
            end_time = None

        if end_time:
            end_time = self._to_iso_8601(end_time)

        task_location = None
        try:
            if hasattr(task, 'file_path') and task.file_path:
                task_location = get_location(task.file_path)
            else:
                task_location = get_location(task.dag.fileloc)
        except Exception as e:
            self.log.warning(
                f'Unable to fetch the location: {e}')

        steps_metadata = []
        if extractor:
            try:
                self.log.info(
                    f'Using extractor {extractor.__name__} '
                    f'task_type={task.__class__.__name__} '
                    f'airflow_dag_id={self.dag_id} '
                    f'task_id={task.task_id} '
                    f'airflow_run_id={dag_run_id} '
                    f'marquez_namespace={self.marquez_namespace}')
                steps_metadata = add_airflow_info_to(
                    task,
                    extractor(task).extract()
                )
            except Exception as e:
                self.log.error(
                    f'Failed to extract metadata {e} '
                    f'airflow_dag_id={self.dag_id} '
                    f'task_id={task.task_id} '
                    f'airflow_run_id={dag_run_id} '
                    f'marquez_namespace={self.marquez_namespace}')
        else:
            self.log.warning(
                f'Unable to find an extractor. '
                f'task_type={task.__class__.__name__} '
                f'airflow_dag_id={self.dag_id} '
                f'task_id={task.task_id} '
                f'airflow_run_id={dag_run_id} '
                f'marquez_namespace={self.marquez_namespace}')

        task_name = f'{self.dag_id}.{task.task_id}'

        # If no extractor found or failed to extract metadata,
        # report the task metadata
        if not steps_metadata:
            steps_metadata = add_airflow_info_to(
                task,
                [StepMetadata(name=task_name)]
            )

        # store all the JobRuns associated with a task
        marquez_jobrun_ids = []

        for step in steps_metadata:
            self.log.info(f'step: {step}')
            inputs = []
            try:
                self.register_datasets(step.inputs)
                inputs = list(
                    map(lambda input: {
                        'namespace': self.marquez_namespace,
                        'name': input.name
                    }, step.inputs))
                self.log.info(f'inputs: {inputs}')
            except Exception as e:
                self.log.error(
                    f'Failed to register inputs: {e} '
                    f'inputs={str(step.inputs)} '
                    f'airflow_dag_id={self.dag_id} '
                    f'task_id={task.task_id} '
                    f'step={step.name} '
                    f'airflow_run_id={dag_run_id} '
                    f'marquez_namespace={self.marquez_namespace}')

            outputs = []
            try:
                self.register_datasets(step.outputs, marquez_job_run_id)
                outputs = list(
                    map(lambda output: {
                        'namespace': self.marquez_namespace,
                        'name': output.name
                    }, step.outputs))
                self.log.info(f'outputs: {outputs}')
            except Exception as e:
                self.log.error(
                    f'Failed to register outputs: {e} '
                    f'outputs={str(step.outputs)} '
                    f'airflow_dag_id={self.dag_id} '
                    f'task_id={task.task_id} '
                    f'step={step.name} '
                    f'airflow_run_id={dag_run_id} '
                    f'marquez_namespace={self.marquez_namespace}')

            marquez_client.create_job(job_name=step.name,
                                      job_type=JobType.BATCH,  # job type
                                      location=(step.location or
                                                task_location),
                                      input_dataset=inputs,
                                      output_dataset=outputs,
                                      context=step.context,
                                      description=self.description,
                                      namespace_name=self.marquez_namespace)
            self.log.info(
                f'Successfully recorded job: {step.name} '
                f'airflow_dag_id={self.dag_id} '
                f'marquez_namespace={self.marquez_namespace}')

            # NOTE: When we have a run ID generated by Marquez, skip creating
            # a new run as we only want to update the dataset with the existing
            # run ID
            if marquez_job_run_id:
                return

            # TODO: Look into generating a uuid based on the DAG run_id
            external_run_id = str(uuid4())

            marquez_client.create_job_run(
                namespace_name=self.marquez_namespace,
                job_name=step.name,
                run_id=external_run_id,
                run_args=run_args,
                nominal_start_time=start_time,
                nominal_end_time=end_time)

            if external_run_id:
                marquez_jobrun_ids.append(external_run_id)
                marquez_client.mark_job_run_as_started(run_id=external_run_id)
            else:
                self.log.error(
                    f'Failed to get run id: {step.name} '
                    f'airflow_dag_id={self.dag_id} '
                    f'airflow_run_id={dag_run_id} '
                    f'marquez_namespace={self.marquez_namespace}')
            self.log.info(
                f'Successfully recorded job run: {step.name} '
                f'airflow_dag_id={self.dag_id} '
                f'airflow_dag_execution_time={start_time} '
                f'marquez_run_id={external_run_id} '
                f'marquez_namespace={self.marquez_namespace} '
                f'duration_ms={(self._now_ms() - report_job_start_ms)}')

        # Store the mapping for all the steps associated with a task
        try:
            self._job_id_mapping.set(
                JobIdMapping.make_key(task_name, dag_run_id),
                json.dumps(marquez_jobrun_ids))

        except Exception as e:
            self.log.error(
                f'Failed to set id mapping : {e} '
                f'airflow_dag_id={self.dag_id} '
                f'task_id={task.task_id} '
                f'airflow_run_id={dag_run_id} '
                f'marquez_run_id={marquez_jobrun_ids} '
                f'marquez_namespace={self.marquez_namespace}')

    def compute_endtime(self, execution_date):
        return self.following_schedule(execution_date)

    def report_jobrun_change(self, ti, run_id, **kwargs):
        job_name = f'{ti.dag_id}.{ti.task_id}'
        session = kwargs.get('session')
        marquez_job_run_ids = self._job_id_mapping.pop(
            JobIdMapping.make_key(job_name, run_id), session)

        if marquez_job_run_ids:
            self.log.info(
                f'Found job runs: '
                f'airflow_dag_id={self.dag_id} '
                f'airflow_job_id={job_name} '
                f'airflow_run_id={run_id} '
                f'marquez_run_ids={marquez_job_run_ids} '
                f'marquez_namespace={self.marquez_namespace}')

            ids = json.loads(marquez_job_run_ids)
            if kwargs.get('success'):
                for marquez_job_run_id in ids:
                    for task_id, task in self.task_dict.items():
                        if task_id == ti.task_id:
                            self.log.info(f'task_id: {task_id}')
                            self.report_task(
                                run_id,
                                None,
                                task,
                                self._extractors.get(task.__class__),
                                marquez_job_run_id=marquez_job_run_id)
                    self.get_or_create_marquez_client(). \
                        mark_job_run_as_completed(run_id=marquez_job_run_id)
            else:
                for marquez_job_run_id in ids:
                    self.get_or_create_marquez_client().mark_job_run_as_failed(
                        run_id=marquez_job_run_id)

        state = 'COMPLETED' if kwargs.get('success') else 'FAILED'
        self.log.info(
            f'Marked job run(s) as {state}. '
            f'airflow_dag_id={self.dag_id} '
            f'airflow_job_id={job_name} '
            f'airflow_run_id={run_id} '
            f'marquez_run_id={marquez_job_run_ids} '
            f'marquez_namespace={self.marquez_namespace}')

    def get_or_create_marquez_client(self):
        if not self._marquez_client:
            self._marquez_client = Clients.new_write_only_client()
        return self._marquez_client

    @staticmethod
    def _now_ms():
        return int(round(time.time() * 1000))

    def register_datasets(self, datasets, marquez_job_run_id=None):
        client = self.get_or_create_marquez_client()
        for dataset in datasets:
            if isinstance(dataset, Dataset):
                _key = f'{self.marquez_namespace}.{dataset.name}'
                if _key not in self._marquez_dataset_cache:
                    self.register_source(dataset.source)
                    client.create_dataset(
                        dataset_name=dataset.name,
                        dataset_type=dataset.type,
                        physical_name=dataset.name,
                        source_name=dataset.source.name,
                        namespace_name=self.marquez_namespace,
                        run_id=marquez_job_run_id)
                    # NOTE:
                    self._marquez_dataset_cache[_key] = True

    def register_source(self, source):
        if isinstance(source, Source):
            _key = source.name
            if _key not in self._marquez_source_cache:
                client = self.get_or_create_marquez_client()
                client.create_source(
                    source.name,
                    source.type,
                    source.connection_url)
                # NOTE:
                self._marquez_source_cache[_key] = True

    @staticmethod
    def _to_iso_8601(dt):
        if isinstance(dt, Pendulum):
            return dt.format(_NOMINAL_TIME_FORMAT)
        else:
            return dt.strftime(_NOMINAL_TIME_FORMAT)

    @staticmethod
    def _get_location(task):
        try:
            if hasattr(task, 'file_path') and task.file_path:
                return get_location(task.file_path)
            else:
                return get_location(task.dag.fileloc)
        except Exception as e:
            DAG.log.warning(f'Unable to fetch the location. {e}')
