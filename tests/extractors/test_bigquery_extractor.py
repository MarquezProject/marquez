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
import logging
import random
import unittest
from datetime import datetime

import mock
import pytz
from airflow.utils import timezone
from airflow.contrib.operators.bigquery_operator import BigQueryOperator
from airflow.models import TaskInstance, DAG
from airflow.utils.state import State

from marquez_airflow.extractors.bigquery_extractor import BigQueryExtractor

log = logging.getLogger(__name__)


class TestBigQueryExtractorE2E(unittest.TestCase):
    def setUp(self):
        log.debug("TestBigQueryExtractorE2E.setup(): ")

    @mock.patch('airflow.contrib.operators.bigquery_operator.BigQueryHook')
    @mock.patch('google.cloud.bigquery.Client')
    def test_extract(self, mock_client, mock_hook):
        log.info("test_extractor")

        job_details_file = open(
            file="tests/extractors/job_details.json",
            mode="r"
        )
        job_details = json.loads(job_details_file.read())
        job_details_file.close()

        bq_job_id = "foo.bq.job_id"

        mock_hook.return_value \
            .get_conn.return_value \
            .cursor.return_value \
            .run_query.return_value = bq_job_id

        mock_client.return_value \
            .get_job.return_value \
            ._properties = job_details

        mock_client.return_value.close.return_value

        mock.seal(mock_hook)
        mock.seal(mock_client)

        dag = DAG(dag_id='TestBigQueryExtractorE2E')
        task = BigQueryOperator(
            sql='select first_name, last_name from customers;',
            task_id="task_id",
            project_id="project_id",
            dag_id="dag_id",
            dag=dag,
            start_date=timezone.datetime(2016, 2, 1, 0, 0, 0)
        )

        task_instance = TaskInstance(
            task=task,
            execution_date=datetime.utcnow().replace(tzinfo=pytz.utc))

        bq_extractor = BigQueryExtractor(task)

        steps_meta = bq_extractor.extract()

        assert steps_meta[0].inputs is not None
        dataset = steps_meta[0].inputs[0]
        assert 'customers' == dataset.name

        assert steps_meta[0].name == "TestBigQueryExtractorE2E.task_id"
        assert steps_meta[0].outputs == []
        assert steps_meta[0].context is not None
        assert steps_meta[0].context["sql"] == task.sql

        task_instance.run()

        steps_meta = bq_extractor.extract_on_complete(task_instance)
        assert steps_meta[0].context['bigquery.job_properties'] \
            == json.dumps(job_details)
        mock_client.return_value \
            .get_job.assert_called_once_with(job_id=bq_job_id)

        assert steps_meta[0].inputs is not None
        assert len(steps_meta[0].inputs) == 1
        assert steps_meta[0].inputs[0].name == \
            "bigquery-public-data.usa_names.usa_1910_2013"
        assert steps_meta[0].outputs is not None
        assert len(steps_meta[0].outputs) == 1
        assert steps_meta[0].outputs[0].name == \
            "bq-airflow-marquez" + \
            "._caa03048f8548c01c38d7ce7ed96d73410a3b7be" + \
            ".anon8adc631eed203758a0ce6505c735e5ceb49d6de7"

        mock_client.return_value.close.assert_called()


class TestBigQueryExtractor(unittest.TestCase):
    def setUp(self):
        log.debug("TestBigQueryExtractor.setup(): ")
        self.task = TestBigQueryExtractor._get_bigquery_task()
        self.ti = TestBigQueryExtractor._get_ti(task=self.task)
        self.bq_extractor = BigQueryExtractor(operator=self.task)

    def test_extract(self):
        log.info("test_extractor")

        steps_meta = BigQueryExtractor(self.task).extract()

        dataset = steps_meta[0].inputs[0]
        assert 'customers' == dataset.name

        assert steps_meta[0].name == "TestBigQueryExtractorE2E.task_id"
        assert steps_meta[0].location is None
        assert steps_meta[0].inputs is not None
        assert steps_meta[0].outputs == []
        assert steps_meta[0].context is not None
        assert steps_meta[0].context["sql"] == self.task.sql

    @mock.patch("airflow.models.TaskInstance.xcom_pull")
    def test_get_bigquery_job_id(self, mock_xcom_pull):
        self.bq_extractor._get_bigquery_job_id(self.ti)

        mock_xcom_pull.assert_called_once_with(
            task_ids=self.ti.task_id, key='job_id')

    @staticmethod
    def _get_ti(task):
        task_instance = TaskInstance(
            task=task,
            execution_date=datetime.utcnow().replace(tzinfo=pytz.utc),
            state=State.RUNNING)
        task_instance.job_id = random.randrange(10000)

        return task_instance

    @staticmethod
    def _get_async_job(properties):
        # BigQuery Job
        class AsyncJob:
            _properties = None

            def __init__(self, _properties):
                self._properties = _properties

        return AsyncJob(_properties=properties)

    @staticmethod
    def _get_bigquery_task():
        dag = DAG(dag_id='TestBigQueryExtractorE2E')
        task = BigQueryOperator(
            sql='select first_name, last_name from customers;',
            task_id="task_id",
            project_id="project_id",
            dag_id="dag_id",
            dag=dag,
            start_date=timezone.datetime(2016, 2, 1, 0, 0, 0)
        )

        return task


if __name__ == '__main__':
    unittest.main()
