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
from uuid import uuid4

import airflow.models
import time
from airflow import LoggingMixin
from airflow.contrib.operators.bigquery_operator import BigQueryOperator
from airflow.operators.postgres_operator import PostgresOperator
from airflow.utils.state import State
from marquez_client import Clients
from marquez_client.models import JobType
from pendulum import Pendulum

from marquez_airflow.extractors import (Dataset, Source, StepMetadata)
from marquez_airflow.extractors.bigquery_extractor import BigQueryExtractor
from marquez_airflow.extractors.postgres_extractor import PostgresExtractor
from marquez_airflow.utils import (
    JobIdMapping,
    get_location,
    add_airflow_info_to
)

_NOMINAL_TIME_FORMAT = "%Y-%m-%dT%H:%M:%S.%fZ"

_DAG_DEFAULT_NAMESPACE = 'default'
_DAG_DEFAULT_OWNER = 'anonymous'

log = logging.getLogger(__name__)


class DAG(airflow.models.DAG, LoggingMixin):
    DEFAULT_NAMESPACE = 'default'
    _job_id_mapping = None
    _marquez_client = None

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
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
        dag_run_id = dagrun.run_id

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
            self.get_or_create_marquez_client() \
                .create_namespace(self.marquez_namespace, _DAG_DEFAULT_OWNER)

            self.log.debug(f"self.task_dict: {self.task_dict}")
            # Register each task in the DAG
            for task_id, task in self.task_dict.items():
                t = self._now_ms()
                try:
                    self.report_task(
                        dag_run_id,
                        execution_date,
                        task,
                        self._get_extractor(task),
                        run_args=run_args)
                except Exception as e:
                    self.log.error(
                        f'Failed to record task {task_id}: {e} '
                        f'{self._timed_log_message(t)}',
                        exc_info=True)

            self.log.info(
                f'Successfully recorded metadata: '
                f'{self._timed_log_message(create_dag_start_ms)}'
            )

        except Exception as e:
            self.log.error(
                f'Failed to record metadata: {e} '
                f'{self._timed_log_message(create_dag_start_ms)}',
                exc_info=True)

        return dagrun

    def _get_extractor(self, task):
        extractor = self._extractors.get(task.__class__)
        log.debug(f'extractor for {task.__class__} is {extractor}')
        return extractor

    def _timed_log_message(self, start_time):
        return f'airflow_dag_id={self.dag_id} ' \
            f'marquez_namespace={self.marquez_namespace} ' \
            f'duration_ms={(self._now_ms() - start_time)}'

    def _handle_task_state(self, run_id, ti):
        if ti.state in {State.SUCCESS, State.SKIPPED}:
            self.log.debug(f"Setting success: {ti.task_id}")
            self.get_or_create_marquez_client(). \
                mark_job_run_as_completed(run_id=run_id)
        else:
            self.log.debug(f"Setting failed: {ti.task_id}")
            self.get_or_create_marquez_client().mark_job_run_as_failed(
                run_id=run_id)

    def handle_callback(self, *args, **kwargs):
        self.log.debug(f"handle_callback({args}, {kwargs})")
        run_args = {
            'external_trigger': kwargs.get('external_trigger', False)
        }
        try:
            dagrun = args[0]
            self.log.debug(f"handle_callback() dagrun : {dagrun}")
            task_instances = dagrun.get_task_instances()

            for ti in task_instances:
                task = self.get_task(ti.task_id)

                extractor = self._get_extractor(task)
                # Emit task started as seen by task

                session = kwargs.get('session')
                run_ids = self._get_run_ids(ti, dagrun.run_id, session)
                if run_ids is None:
                    run_ids = []
                if extractor:
                    steps_meta = add_airflow_info_to(
                        task,
                        extractor(task).extract_on_complete(ti))
                    for step in steps_meta:
                        self.log.debug(f'step: {step}')
                        if not run_ids:
                            run_id = self._begin_run_flow(step,
                                                          self._get_location(
                                                              task),
                                                          self._to_iso_8601(
                                                              ti.start_date),
                                                          self._to_iso_8601(
                                                              ti.end_date),
                                                          run_args)
                            run_ids.append(run_id)

                        for run_id in run_ids:
                            self._end_run_flow(step, self._get_location(task),
                                               run_id,
                                               self._to_iso_8601(
                                                   ti.start_date),
                                               ti.state)
                            self._handle_task_state(run_id, ti)
                else:
                    if run_ids is None:
                        continue
                    for run_id in run_ids:
                        self.get_or_create_marquez_client() \
                            .mark_job_run_as_started(run_id, self._to_iso_8601(
                                ti.start_date))
                        self._handle_task_state(run_id, ti)

            return
        except Exception as e:
            self.log.error(
                f'Failed to record dagrun state change: {e} '
                f'dag_id={self.dag_id}',
                exc_info=True)

        return super().handle_callback(*args, **kwargs)

    def _register_dataset(self, step_dataset, run_id=None):
        inputs = None
        if step_dataset:
            self.register_datasets(step_dataset, run_id)
            inputs = self._to_dataset_ids(step_dataset)
        return inputs

    def _begin_run_flow(self, step, location, start_date, end_date,
                        run_args={}):
        # Create a job to ensure it exists
        self.get_or_create_marquez_client().create_job(
            namespace_name=self.marquez_namespace,
            job_name=step.name,
            job_type=JobType.BATCH,
            location=step.location or location,
            input_dataset=self._register_dataset(step.inputs),
            output_dataset=self._register_dataset(step.outputs),
            context=step.context or {},
            description=self.description)
        self.log.info(start_date)
        # Run must be called after job creation
        run_id = self.create_run(step.name, run_args,
                                 start_date,
                                 end_date)
        self.log.info(f"Created run: {run_id}")
        return run_id

    def _end_run_flow(self, step, location, run_id, actual_start_date,
                      ti_state):
        if actual_start_date:
            self.get_or_create_marquez_client() \
                .mark_job_run_as_started(run_id, actual_start_date)
        # Recreate job to associate run
        self.get_or_create_marquez_client().create_job(
            namespace_name=self.marquez_namespace,
            job_name=step.name,
            job_type=JobType.BATCH,
            location=step.location or location,
            input_dataset=self._register_dataset(step.inputs),
            output_dataset=self._register_dataset(step.outputs,
                                                  run_id),
            context=step.context or {},
            description=self.description,
            run_id=run_id)

    def _get_run_ids(self, ti, run_id, session):
        job_name = self._marquez_job_name_from_ti(ti)

        run_ids = self._job_id_mapping.pop(
            job_name, run_id, session)
        return run_ids

    def _to_dataset_ids(self, datasets):
        return list(map(lambda ds: {
            'namespace': self.marquez_namespace,
            'name': ds.name
        }, datasets))

    def report_task(self,
                    dag_run_id,
                    execution_date,
                    task,
                    extractor,
                    marquez_job_run_id=None,
                    run_args=None):

        task_location = self._get_location(task)

        task_info = f'task_type={task.__class__.__name__} ' \
            f'airflow_dag_id={self.dag_id} ' \
            f'task_id={task.task_id} ' \
            f'airflow_run_id={dag_run_id} ' \
            f'marquez_namespace={self.marquez_namespace}'

        steps_metadata = None
        if extractor:
            try:
                self.log.debug(
                    f'Using extractor {extractor.__name__} {task_info}')

                steps_metadata = add_airflow_info_to(
                    task,
                    extractor(task).extract()
                )
            except Exception as e:
                self.log.error(
                    f'Failed to extract metadata {e} {task_info}',
                    exc_info=True)
        else:
            self.log.warning(
                f'Unable to find an extractor. {task_info}')

        task_name = self._marquez_job_name(self.dag_id, task.task_id)

        # If no extractor found or failed to extract metadata,
        # report the task metadata
        if steps_metadata is None:
            steps_metadata = add_airflow_info_to(
                task,
                [StepMetadata(name=task_name)]
            )
        # store all the JobRuns associated with a task
        marquez_jobrun_ids = []

        for step in steps_metadata:
            run_id = self._begin_run_flow(step, task_location,
                                          self._get_start_time(execution_date),
                                          self._get_end_time(execution_date),
                                          run_args)
            marquez_jobrun_ids.append(run_id)

        # Store the mapping for all the steps associated with a task
        try:
            self._job_id_mapping.set(
                task_name, dag_run_id,
                marquez_jobrun_ids)

        except Exception as e:
            self.log.error(
                f'Failed to set id mapping : {e} {task_info}',
                exc_info=True)

    def _get_start_time(self, execution_date):
        if execution_date:
            return self._to_iso_8601(execution_date)
        else:
            return None

    def _get_end_time(self, execution_date):
        if execution_date:
            end_time = self.compute_endtime(execution_date)
        else:
            end_time = None

        if end_time:
            end_time = self._to_iso_8601(end_time)
        return end_time

    def create_run(self, job_name, run_args, start_time, end_time) -> str:
        marquez_client = self.get_or_create_marquez_client()
        external_run_id = self.new_run_id()
        marquez_client.create_job_run(
            namespace_name=self.marquez_namespace,
            job_name=job_name,
            run_id=external_run_id,
            run_args=run_args,
            nominal_start_time=start_time,
            nominal_end_time=end_time)
        return external_run_id

    def compute_endtime(self, execution_date):
        return self.following_schedule(execution_date)

    def get_or_create_marquez_client(self):
        if not self._marquez_client:
            self._marquez_client = Clients.new_write_only_client()
        return self._marquez_client

    def new_run_id(self) -> str:
        return str(uuid4())

    @staticmethod
    def _now_ms():
        return int(round(time.time() * 1000))

    def register_datasets(self, datasets, marquez_job_run_id=None):
        client = self.get_or_create_marquez_client()
        for dataset in datasets:
            if isinstance(dataset, Dataset):
                self.register_source(dataset.source)
                # NOTE: The client expects a dict when capturing
                # fields for a dataset. Below we translate a field
                # object into a dict for compatibility. Work is currently
                # in progress to make this step unnecessary (see:
                # https://github.com/MarquezProject/marquez-python/pull/89)
                fields = []
                for field in dataset.fields:
                    fields.append({
                        'name': field.name,
                        'type': field.type,
                        'tags': field.tags,
                        'description': field.description
                    })
                client.create_dataset(
                    dataset_name=dataset.name,
                    dataset_type=dataset.type,
                    physical_name=dataset.name,
                    source_name=dataset.source.name,
                    namespace_name=self.marquez_namespace,
                    fields=fields,
                    run_id=marquez_job_run_id)

    def register_source(self, source):
        if isinstance(source, Source):
            _key = source.name
            if _key not in self._marquez_source_cache:
                client = self.get_or_create_marquez_client()
                client.create_source(
                    source.name,
                    source.type,
                    source.connection_url)
                self._marquez_source_cache[_key] = True

    @staticmethod
    def _to_iso_8601(dt):
        if not dt:
            return None
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
        except Exception:
            log.warning(f"Failed to get location for task '{task.task_id}'.",
                        exc_info=True)
            return None

    @staticmethod
    def _marquez_job_name_from_ti(ti):
        return DAG._marquez_job_name(ti.dag_id, ti.task_id)

    @staticmethod
    def _marquez_job_name(dag_id, task_id):
        return f'{dag_id}.{task_id}'
