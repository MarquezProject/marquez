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

from marquez_codegen_client import (
    ApiClient, Configuration, CreateJob,
    CreateJobRun, CreateNamespace, DatasetsApi, DatasourcesApi,
    JobsApi, NamespacesApi)


class MarquezClient(object):
    API_PATH = "api/v1"
    DEFAULT_TIMEOUT_SEC = 5
    c = None
    namespace = None

    dataset_api_client = None
    datasource_api_client = None
    jobs_api_client = None
    namespace_api_client = None

    def __init__(self, host=None, port=None, timeout=None):
        self.host = os.environ.get('MARQUEZ_HOST') or host
        self.port = os.environ.get('MARQUEZ_PORT') or (
            str(port) if port else port)
        t = os.environ.get('MARQUEZ_TIMEOUT') or str(timeout)
        self.timeout = int(t) if t.isnumeric() else self.DEFAULT_TIMEOUT_SEC

        if not self.host or not self.port:
            msg = ("Please provide host & port or set the proper env "
                   "vars: MARQUEZ_HOST, MARQUEZ_PORT")
            raise ConnectionError(msg)

        logging.info("Connecting to Marquez at {}:{}".format(self.host,
                                                             self.port))
        c = Configuration()
        c.host = "{0}:{1}/{2}".format(self.host, self.port, self.API_PATH)

        # create an instance of the API class
        marquez_client_instance = ApiClient(c)
        self.dataset_api_client = DatasetsApi(marquez_client_instance)
        self.datasource_api_client = DatasourcesApi(marquez_client_instance)
        self.jobs_api_client = JobsApi(marquez_client_instance)
        self.namespace_api_client = NamespacesApi(marquez_client_instance)

    #  Namespace API
    def set_namespace(self, namespace, owner=None, description=None):
        self._create_namespace(namespace, owner or 'default', description)
        self.namespace = namespace

    def get_namespace(self):
        if not self.namespace:
            raise Exception("No namespace set.")
        return self.namespace

    def get_namespace_info(self, ns_name):
        if not ns_name:
            raise ValueError("Please provide a namespace")
        return self.namespace_api_client.namespaces_namespace_get(
            ns_name,
            _request_timeout=self.timeout
        )

    def _create_namespace(self, namespace,
                          namespace_owner, namespace_description=None):
        create_namespace_request = CreateNamespace(
            namespace_owner, namespace_description)
        response = self.namespace_api_client.namespaces_namespace_put(
            namespace, create_namespace=create_namespace_request,
            _request_timeout=self.timeout
        )
        return response

    #  Jobs API
    def create_job(self, job_name, location, input_dataset_urns,
                   output_dataset_urns, description=None):
        create_job_request = CreateJob(
            input_dataset_urns, output_dataset_urns, location, description)
        created_job = self.jobs_api_client.namespaces_namespace_jobs_job_put(
            self.get_namespace(),
            job_name, create_job=create_job_request,
            _request_timeout=self.timeout
        )
        return created_job

    def create_job_run(self, job_name, job_run_args,
                       nominal_start_time=None, nominal_end_time=None):
        job_run_creation_request = CreateJobRun(
            nominal_start_time, nominal_end_time, job_run_args)
        return self.jobs_api_client.namespaces_namespace_jobs_job_runs_post(
            self.get_namespace(),
            job_name,
            create_job_run=job_run_creation_request,
            _request_timeout=self.timeout
        )

    def get_job_run(self, job_run_id):
        return self.jobs_api_client.jobs_runs_id_get(
            job_run_id,
            _request_timeout=self.timeout
        )

    def mark_job_run_running(self, job_run_id):
        self.jobs_api_client.jobs_runs_id_run_put(
            job_run_id,
            _request_timeout=self.timeout
        )

    def mark_job_run_completed(self, job_run_id):
        self.jobs_api_client.jobs_runs_id_complete_put(
            job_run_id,
            _request_timeout=self.timeout
        )

    def mark_job_run_failed(self, job_run_id):
        self.jobs_api_client.jobs_runs_id_fail_put(
            job_run_id,
            _request_timeout=self.timeout
        )

    def mark_job_run_aborted(self, job_run_id):
        self.jobs_api_client.jobs_runs_id_abort_put(
            job_run_id,
            _request_timeout=self.timeout
        )

    def get_all_datasources(self):
        return self.datasource_api_client.datasources_get(
            _request_timeout=self.timeout)

    def get_datasource(self, urn):
        return self.datasource_api_client.datasources_urn_get(
            urn=urn,
            _request_timeout=self.timeout)

    def create_datasource(self, create_datasource_request):
        return self.datasource_api_client.datasources_post(
            create_datasource=create_datasource_request,
            _request_timeout=self.timeout)
