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

_NOMINAL_TIME_FORMAT = "%Y-%m-%dT%H:%M:%SZ"

_DAG_DEFAULT_NAMESPACE = 'default'
_DAG_DEFAULT_OWNER = 'anonymous'

log = logging.getLogger(__name__)


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
        log.info(f'extractor for {task.__class__} is {extractor}')
        return extractor

    def _timed_log_message(self, start_time):
        return f'airflow_dag_id={self.dag_id} ' \
            f'marquez_namespace={self.marquez_namespace} ' \
            f'duration_ms={(self._now_ms() - start_time)}'

    def _handle_task_state(self, marquez_job_run_ids, ti):
        for marquez_job_run_id in marquez_job_run_ids:
            if ti.state in {State.SUCCESS, State.SKIPPED}:
                self.log.info(f"Setting success: {ti.task_id}")
                self.get_or_create_marquez_client(). \
                    mark_job_run_as_completed(run_id=marquez_job_run_id)
            else:
                self.log.info(f"Setting failed: {ti.task_id}")
                self.get_or_create_marquez_client().mark_job_run_as_failed(
                    run_id=marquez_job_run_id)

    def handle_callback(self, *args, **kwargs):
        self.log.debug(f"handle_callback({args}, {kwargs})")

        try:
            dagrun = args[0]
            self.log.debug(f"handle_callback() dagrun : {dagrun}")
            task_instances = dagrun.get_task_instances()
            self.log.info(f"{task_instances}")

            for ti in task_instances:
                ti.task = self.get_task(ti.task_id)
                task = ti.task

                extractor = self._get_extractor(task)
                self.log.info(f"{ti}")
                job_name = self._marquez_job_name_from_ti(ti)
                session = kwargs.get('session')
                marquez_job_run_ids = self._job_id_mapping.pop(
                    job_name, dagrun.run_id, session)
                if marquez_job_run_ids is None:
                    self.log.error(f'No runs assocated with task {ti}')
                    continue

                if extractor:
                    steps_meta = add_airflow_info_to(
                        task,
                        extractor(task).extract_on_complete(ti))

                    for step in steps_meta:
                        self.log.info(f'step: {step}')

                        for marquez_run_id in marquez_job_run_ids:
                            self.log.info(f'marquez_run_id: {marquez_run_id}')

                            inputs = None
                            if step.inputs is not None:
                                self.register_datasets(step.inputs)
                                inputs = self._to_dataset_ids(step.inputs)
                                self.log.info(
                                    f'inputs: {inputs} '
                                )
                            outputs = None
                            if step.outputs is not None:
                                self.register_datasets(step.outputs,
                                                       marquez_run_id)
                                outputs = self._to_dataset_ids(step.outputs)
                                self.log.info(
                                    f'outputs: {outputs} '
                                )

                            ti_location = self._get_location(task)
                            self.get_or_create_marquez_client().create_job(
                                namespace_name=self.marquez_namespace,
                                job_name=step.name,
                                job_type=JobType.BATCH,
                                location=step.location or ti_location,
                                input_dataset=inputs,
                                output_dataset=outputs,
                                description=self.description,
                                context=step.context,
                                run_id=marquez_run_id)

                            self.log.info(f"client.create_job(run_id="
                                          f"{marquez_run_id}) successful.")
                self._handle_task_state(marquez_job_run_ids, ti)
            return
        except Exception as e:
            self.log.error(
                f'Failed to record dagrun state change: {e} '
                f'dag_id={self.dag_id}',
                exc_info=True)

        return super().handle_callback(*args, **kwargs)

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

        task_location = self._get_location(task)

        task_info = f'task_type={task.__class__.__name__} ' \
            f'airflow_dag_id={self.dag_id} ' \
            f'task_id={task.task_id} ' \
            f'airflow_run_id={dag_run_id} ' \
            f'marquez_namespace={self.marquez_namespace}'

        steps_metadata = []
        if extractor:
            try:
                self.log.info(
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
                inputs = self._to_dataset_ids(step.inputs)
                self.log.info(f'inputs: {inputs}')
            except Exception as e:
                self.log.error(
                    f'Failed to register inputs: {e} {task_info}'
                    f'inputs={str(step.inputs)} '
                    f'step={step.name} ',
                    exc_info=True)

            outputs = []
            try:
                self.register_datasets(step.outputs, marquez_job_run_id)
                outputs = self._to_dataset_ids(step.outputs)
                self.log.info(f'outputs: {outputs}')
            except Exception as e:
                self.log.error(
                    f'Failed to register outputs: {e} {task_info}'
                    f'outputs={str(step.outputs)} ',
                    exc_info=True)

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
                f'Successfully recorded job: {step.name} {task_info}'
                )

            # NOTE: When we have a run ID generated by Marquez, skip creating
            # a new run as we only want to update the dataset with the existing
            # run ID
            if marquez_job_run_id:
                return

            # TODO: Look into generating a uuid based on the DAG run_id
            external_run_id = self.new_run_id()

            marquez_client.create_job_run(
                namespace_name=self.marquez_namespace,
                job_name=step.name,
                run_id=external_run_id,
                run_args=run_args,
                nominal_start_time=start_time,
                nominal_end_time=end_time)

            marquez_jobrun_ids.append(external_run_id)
            marquez_client.mark_job_run_as_started(run_id=external_run_id)

            self.log.info(
                f'Successfully recorded job run: {step.name} {task_info}'
                f'airflow_dag_execution_time={start_time} '
                f'marquez_run_id={external_run_id} '
                f'duration_ms={(self._now_ms() - report_job_start_ms)}')

        # Store the mapping for all the steps associated with a task
        try:
            self._job_id_mapping.set(
                task_name, dag_run_id,
                marquez_jobrun_ids)

        except Exception as e:
            self.log.error(
                f'Failed to set id mapping : {e} {task_info}',
                exc_info=True)

    def compute_endtime(self, execution_date):
        return self.following_schedule(execution_date)

    def get_or_create_marquez_client(self):
        if not self._marquez_client:
            self._marquez_client = Clients.new_write_only_client()
        return self._marquez_client

    def new_run_id(self):
        return str(uuid4())

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
