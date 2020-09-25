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
import unittest
import logging

import mock
from airflow.contrib.operators.bigquery_operator import BigQueryOperator

from marquez_airflow.extractors.bigquery_extractor import BigQueryExtractor
from marquez_airflow.utils import get_job_name


class TestBigQueryExtractor(unittest.TestCase):
    def setUp(self):
        logging.basicConfig(format='%(levelname)s:%(message)s',
                            level=logging.DEBUG)
        logging.debug("TestBigQueryExtractor.setup(): ")

    def test_extract(self):
        logging.info("test_extractor")

        task = BigQueryOperator(
            bql=None,
            sql='select first_name, last_name from customers;',
            destination_dataset_table=None,
            write_disposition='WRITE_EMPTY',
            allow_large_results=False,
            flatten_results=None,
            bigquery_conn_id='bigquery_default',
            delegate_to=None,
            udf_config=None,
            use_legacy_sql=True,
            maximum_billing_tier=None,
            maximum_bytes_billed=None,
            create_disposition='CREATE_IF_NEEDED',
            schema_update_options=(),
            query_params=None,
            labels=None,
            priority='INTERACTIVE',
            time_partitioning=None,
            api_resource_configs=None,
            cluster_fields=None,
            location=None,
            encryption_configuration=None,
            task_id="task_id",
            project_id="project_id",
            dag_id="dag_id",
        )

        # self.task_dict = dict()  # type: Dict[str, BaseOperator]
        # task = ("task_id", BigQueryOperator())

        steps_meta = BigQueryExtractor(task).extract()
        dataset = steps_meta[0].inputs[0]

        assert 'customers' == dataset.name

    @mock.patch("google.cloud.bigquery.Client")
    @mock.patch("json.dumps")
    def test_extract_on_complete(self, mock_json, mock_client):
        logging.info("test_extract_on_complete")

        task = BigQueryOperator(
            bql=None,
            sql='SELECT name '
                'FROM bigquery-public-data.usa_names.usa_1910_2013 '
                'WHERE state = "TX" LIMIT 100',
            destination_dataset_table=None,
            write_disposition='WRITE_EMPTY',
            allow_large_results=False,
            flatten_results=None,
            bigquery_conn_id='bigquery_default',
            delegate_to=None,
            udf_config=None,
            use_legacy_sql=True,
            maximum_billing_tier=None,
            maximum_bytes_billed=None,
            create_disposition='CREATE_IF_NEEDED',
            schema_update_options=(),
            query_params=None,
            labels=None,
            priority='INTERACTIVE',
            time_partitioning=None,
            api_resource_configs=None,
            cluster_fields=None,
            location=None,
            encryption_configuration=None,
            task_id="task_id",
            project_id="project_id",
            dag_id="dag_id",
        )

        job_name = get_job_name(task=task)
        BigQueryExtractor(task).extract_on_complete()

        mock_client.get_job(job_name).return_value = {}
        mock_json.return_value = \
            TestBigQueryExtractor.get_job_details(self)

        assert mock_json.return_value.get("statistics").get(
            "totalBytesProcessed") == "65935918"

    @staticmethod
    def get_job_details(self):
        with open('tests/extractors/job_details.json') as json_file:
            return json.load(json_file)


if __name__ == '__main__':
    unittest.main()
