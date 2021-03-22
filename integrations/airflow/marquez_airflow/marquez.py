import os
import logging
import uuid
from typing import Optional, Dict, Type

from marquez_airflow.extractors import Dataset, StepMetadata
from marquez_airflow.version import VERSION as MARQUEZ_AIRFLOW_VERSION

from openlineage.client import OpenLineageClient
from openlineage.facet import DocumentationJobFacet, SourceCodeLocationJobFacet, SqlJobFacet, \
    DocumentationDatasetFacet, SchemaDatasetFacet, SchemaField, DataSourceDatasetFacet, \
    NominalTimeRunFacet, ParentRunFacet, BaseFacet
from openlineage.run import Dataset as OpenLineageDataset, RunEvent, RunState, Run, Job

_DAG_DEFAULT_OWNER = 'anonymous'
_DAG_DEFAULT_NAMESPACE = 'default'

_DAG_NAMESPACE = os.getenv(
    'MARQUEZ_NAMESPACE', _DAG_DEFAULT_NAMESPACE
)

log = logging.getLogger(__name__)


class MarquezAdapter:
    _client = None

    def get_or_create_marquez_client(self) -> OpenLineageClient:
        if not self._client:
            self._client = OpenLineageClient.from_environment()
        return self._client

    def start_task(
            self,
            job_name: str,
            job_description: str,
            event_time: str,
            parent_run_id: Optional[str],
            code_location: Optional[str],
            nominal_start_time: str,
            nominal_end_time: str,
            step: Optional[StepMetadata],
            run_facets: Optional[Dict[str, Type[BaseFacet]]] = None  # Custom run facets
    ) -> str:
        sql = None
        if step:
            sql = step.context.get('sql', None)

        run_id = self._new_run_id()
        event = RunEvent(
            eventType=RunState.START,
            eventTime=event_time,
            run=self._build_run(
                run_id, parent_run_id, job_name, nominal_start_time, nominal_end_time, run_facets
            ),
            job=self._build_job(
                job_name, job_description, code_location, sql
            ),
            producer=f"marquez-airflow/{MARQUEZ_AIRFLOW_VERSION}",
            inputs=[
                self.map_airflow_dataset(dataset) for dataset in step.inputs
            ] if step else None,
            outputs=[
                self.map_airflow_dataset(dataset) for dataset in step.outputs
            ] if step else None
        )
        self.get_or_create_marquez_client().emit(event)
        return event.run.runId

    def complete_task(
        self,
        run_id: str,
        job_name: str,
        end_time: str,
        step: StepMetadata
    ):
        event = RunEvent(
            eventType=RunState.COMPLETE,
            eventTime=end_time,
            run=self._build_run(
                run_id
            ),
            job=self._build_job(
                job_name
            ),
            inputs=[
                self.map_airflow_dataset(dataset) for dataset in step.inputs
            ],
            outputs=[
                self.map_airflow_dataset(dataset) for dataset in step.outputs
            ],
            producer=f"marquez-airflow/{MARQUEZ_AIRFLOW_VERSION}"
        )
        self.get_or_create_marquez_client().emit(event)

    def fail_task(
        self,
        run_id: str,
        job_name: str,
        end_time: str,
        step: StepMetadata
    ):
        event = RunEvent(
            eventType=RunState.FAIL,
            eventTime=end_time,
            run=self._build_run(
                run_id
            ),
            job=self._build_job(
                job_name
            ),
            inputs=[
                self.map_airflow_dataset(dataset) for dataset in step.inputs
            ],
            outputs=[
                self.map_airflow_dataset(dataset) for dataset in step.outputs
            ],
            producer=f"marquez-airflow/{MARQUEZ_AIRFLOW_VERSION}"
        )
        self.get_or_create_marquez_client().emit(event)

    @staticmethod
    def _build_run(
            run_id: str,
            parent_run_id: Optional[str] = None,
            job_name: Optional[str] = None,
            nominal_start_time: Optional[str] = None,
            nominal_end_time: Optional[str] = None,
            custom_facets: Dict[str, Type[BaseFacet]] = None
    ) -> Run:
        facets = {}
        if nominal_start_time:
            facets.update({
                "nominalTime": NominalTimeRunFacet(nominal_start_time, nominal_end_time)
            })
        if parent_run_id:
            facets.update({"parentRun": ParentRunFacet.create(
                parent_run_id,
                _DAG_NAMESPACE,
                job_name
            )})

        if custom_facets:
            facets.update(custom_facets)

        return Run(run_id, facets)

    @staticmethod
    def _build_job(
            job_name: str,
            job_description: Optional[str] = None,
            code_location: Optional[str] = None,
            sql: Optional[str] = None
    ):
        facets = {}

        if job_description:
            facets.update({
                "documentation": DocumentationJobFacet(job_description)
            })
        if code_location:
            facets.update({
                "sourceCodeLocation": SourceCodeLocationJobFacet("", code_location)
            })
        if sql:
            facets.update({
                "sql": SqlJobFacet(sql)
            })

        return Job(_DAG_NAMESPACE, job_name, facets)

    @staticmethod
    def map_airflow_dataset(dataset: Dataset) -> OpenLineageDataset:
        facets = {
            "dataSource": DataSourceDatasetFacet(
                dataset.source.name,
                dataset.source.connection_url
            )
        }
        if dataset.description:
            facets.update({
                "documentation": DocumentationDatasetFacet(
                    description=dataset.description
                )
            })

        if dataset.fields is not None and len(dataset.fields):
            facets.update({
                "schema": SchemaDatasetFacet(
                    fields=[
                        SchemaField(field.name, field.type, field.description)
                        for field in dataset.fields
                    ]
                )
            })

        return OpenLineageDataset(
            namespace=_DAG_NAMESPACE,
            name=dataset.name,
            facets=facets
        )

    @staticmethod
    def _new_run_id() -> str:
        return str(uuid.uuid4())
