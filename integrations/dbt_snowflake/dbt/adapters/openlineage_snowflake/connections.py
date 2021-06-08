import re
import uuid
from datetime import datetime
from typing import Optional
from dataclasses import dataclass

from dbt.adapters.snowflake import SnowflakeCredentials, SnowflakeConnectionManager

from dbt.adapters.openlineage_snowflake.version import __version__ as VERSION
from dbt.exceptions import DatabaseException
from dbt.logger import GLOBAL_LOGGER as logger

from marquez.sql import SqlParser
from openlineage.client import OpenLineageClientOptions, OpenLineageClient
from openlineage.facet import SourceCodeLocationJobFacet, SqlJobFacet
from openlineage.run import RunEvent, RunState, Run, Job, Dataset


@dataclass
class OpenLineageSnowflakeCredentials(SnowflakeCredentials):
    """setting openlineage_url in dbt profile is required"""
    openlineage_url: Optional[str] = None
    openlineage_timeout: float = 5.0
    openlineage_apikey: Optional[str] = None

    @property
    def type(self) -> str:
        return 'openlineage_snowflake'


@dataclass
class RunMeta:
    """Runtime information about model set up on start event that
    needs to be shared with complete/fail events
    """
    run_id: str
    namespace: str
    job_name: str


BQ_QUERY_JOB_SPLIT = '-----Query Job SQL Follows-----'
PRODUCER = f"dbt-openlineage_snowflake/{VERSION}"


class OpenLineageSnowflakeConnectionManager(SnowflakeConnectionManager):
    """Emitting openlineage events here allows us to handle
    errors and send fail events for them
    """
    TYPE = 'openlineage_snowflake'

    @classmethod
    def handle_error(cls, error, message):
        error_msg = "\n".join([item['message'] for item in error.errors])
        raise DatabaseException(error_msg)

    def get_openlineage_client(self):
        if not hasattr(self, '_openlineage_client'):
            creds = self.profile.credentials
            self._openlineage_client = OpenLineageClient(
                creds.openlineage_url,
                OpenLineageClientOptions(
                    timeout=creds.openlineage_timeout,
                    api_key=creds.openlineage_apikey
                )
            )
        return self._openlineage_client

    def emit_start(self, model, run_started_at: str):
        run_id = str(uuid.uuid4())

        inputs = []
        try:
            sql_meta = SqlParser.parse(model['compiled_sql'], None)
            inputs = [
                in_table.qualified_name for in_table in sql_meta.in_tables
            ]
        except Exception as e:
            logger.error(f"Cannot parse snowflake sql. {e}", exc_info=True)

        connection = self.get_thread_connection()
        namespace = f'snowflake://{connection.credentials.account}'

        # set up metadata information to use in complete/fail events
        if not hasattr(self, '_meta'):
            self._meta = dict()
        meta = RunMeta(
            run_id,
            namespace,
            model['unique_id']
        )
        self._meta[model['unique_id']] = meta
        self._meta[run_id] = meta

        event = RunEvent(
            eventType=RunState.START,
            eventTime=run_started_at,
            run=Run(runId=run_id),
            job=Job(
                namespace=meta.namespace,
                name=meta.job_name,
                facets={
                    "sourceCodeLocation": SourceCodeLocationJobFacet(
                        "",
                        model['original_file_path']
                    ),
                    "sql": SqlJobFacet(model['compiled_sql'])
                }
            ),
            producer=PRODUCER,
            inputs=sorted([
                Dataset(
                    namespace=meta.namespace,
                    name=relation,
                    facets={}
                ) for relation in inputs
            ], key=lambda x: x.name),
            outputs=[
                Dataset(namespace=meta.namespace, name=model['relation_name'])
            ]
        )
        self.get_openlineage_client().emit(event)
        return run_id

    def emit_complete(self, run_id):
        meta = self._meta[run_id]

        self.get_openlineage_client().emit(RunEvent(
            eventType=RunState.COMPLETE,
            eventTime=datetime.now().isoformat(),
            run=Run(runId=run_id),
            job=Job(namespace=meta.namespace, name=meta.job_name),
            producer=PRODUCER,
            inputs=[],
            outputs=[]
        ))
        logger.debug(f'openlineage complete event with run_id: {run_id}')
        return

    def emit_failed(self, sql):
        res = re.search(r'"node_id": "(.*)"', sql)
        if not res:
            logger.error("can't emit OpenLineage event when run failed: can't find run id")
        meta = self._meta[res.group(1)]
        self.get_openlineage_client().emit(RunEvent(
            eventType=RunState.FAIL,
            eventTime=datetime.now().isoformat(),
            run=Run(runId=meta.run_id),
            job=Job(namespace=meta.namespace, name=meta.job_name),
            producer=PRODUCER,
            inputs=[],
            outputs=[]
        ))
        logger.debug(f'openlineage failed event with run_id: {meta.run_id}')
