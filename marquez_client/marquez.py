import json
import os

import logging
from marquez_client import Configuration, ApiClient, DatasetsApi, JobsApi, NamespacesApi, CreateNamespace, CreateJobRun, \
    CreateJob


class MarquezClient(object):
    API_PATH = "api/v1"
    c = None
    namespace = None
    dataset_api_client = None
    jobs_api_client = None
    namespace_api_client = None

    def __init__(self):
        host_from_configs = os.environ['MQZ_HOST']
        port_from_configs = os.environ['MQZ_PORT']

        if not (host_from_configs and port_from_configs):
            raise Exception("Please provide proper env vars in context: MQZ_HOSTNAME and MQZ_PORT")

        logging.info("Connecting to Marquez at %s:%s", host_from_configs, port_from_configs)
        c = Configuration()
        c.host = "{0}:{1}/{2}".format(host_from_configs, port_from_configs, self.API_PATH)

        # create an instance of the API class
        marquez_client_instance = ApiClient(c)
        self.dataset_api_client = DatasetsApi(marquez_client_instance)
        self.jobs_api_client = JobsApi(marquez_client_instance)
        self.namespace_api_client = NamespacesApi(marquez_client_instance)

    def set_namespace(self, namespace, owner=None, description=None):
        self._create_namespace(namespace, owner or 'default', description)
        self.namespace = namespace

    def get_namespace(self):
        if not self.namespace:
            raise Exception("No namespace set.")
        return self.namespace

    def _create_namespace(self, namespace, namespace_owner, namespace_description=None):
        create_namespace_request = CreateNamespace(namespace_owner, namespace_description)
        response = self.namespace_api_client.namespaces_namespace_put(
            namespace, create_namespace=create_namespace_request)
        return response

    def create_job(self, job_name, location, input_dataset_urns,
                   output_dataset_urns, description=None):
        create_job_request = CreateJob(input_dataset_urns, output_dataset_urns, location, description)
        created_job = self.jobs_api_client.namespaces_namespace_jobs_job_put(self.get_namespace(), job_name, create_job=create_job_request)
        return created_job

    def create_job_run(self, job_name, job_run_args, nominal_start_time=None, nominal_end_time=None):
        job_run_creation_request = CreateJobRun(nominal_start_time, nominal_end_time, job_run_args)
        return self.jobs_api_client.namespaces_namespace_jobs_job_runs_post(
            self.get_namespace(), job_name, create_job_run=job_run_creation_request)

    def get_job_run(self, job_run_id):
        return self.jobs_api_client.jobs_runs_id_get(job_run_id)

    def mark_job_run_running(self, job_run_id):
        self.jobs_api_client.jobs_runs_id_run_put(job_run_id)

    def mark_job_run_completed(self, job_run_id):
        self.jobs_api_client.jobs_runs_id_complete_put(job_run_id)

    def mark_job_run_failed(self, job_run_id):
        self.jobs_api_client.jobs_runs_id_fail_put(job_run_id)

    def mark_job_run_aborted(self, job_run_id):
        self.jobs_api_client.jobs_runs_id_abort_put(job_run_id)


if __name__ == "__main__":
    # Namespace Params
    new_namespace = "aleks_ns4"
    owner = "aleks2"
    ns_desc = "aleks_testing_ns"

    # Job Params
    job_name = "aleks_job"
    location = "git://my_fav_project/5gfd53F892KJkx"
    description = "a simple job"
    input_data_set_urns = ["a://b/c", "d://e/f"]

    output_data_set_urns = ["s3://i/j/k"]

    my_app = MarquezClient()
    my_app.set_namespace(new_namespace, owner=owner, description=None)

    job_result = my_app.create_job(job_name, location, input_data_set_urns, output_data_set_urns, description)
    print("Job Creation:")
    print(job_result)

    job_run = my_app.create_job_run(job_name, job_run_args=json.dumps({"k1" : "v1", "k2" : "v2"}))
    run_id = job_run.run_id
    print("Job Run Creation:")
    print(job_run)

    job_run = my_app.get_job_run(run_id)
    print("Getting job run result:")
    print(job_run)
    my_app.mark_job_run_running(run_id)

    job_run = my_app.get_job_run(run_id)
    print("Getting job run result (after marked as running):")
    print(job_run)

    my_app.mark_job_run_completed(run_id)
    job_run = my_app.get_job_run(run_id)
    print("Getting job run result (after marked as completed):")
    print(job_run)