from enum import Enum
from marquez_client import Configuration, CreateNamespace, CreateJob, CreateJobRun, NamespacesApi, ApiClient, JobsApi

class RunState(Enum):
    RUNNING = 1
    COMPLETED = 2
    FAILED = 3
    ABORTED = 4


class MarquezClient:
    namespace = None
    jobs_api_client = None
    namespace_api_client = None

    def __init__(self):
        conf = Configuration()
        conf.host = 'localhost:5000/api/v1'  # TODO make it a set() method

        # create an instance of the API class
        api_client = ApiClient(conf)
        self.jobs_api_client = JobsApi(api_client)
        self.namespace_api_client = NamespacesApi(api_client)

    def set_namespace(self, namespace):
        self.namespace = namespace
        payload = CreateNamespace('anonymous', '')
        return self.namespace_api_client.namespaces_namespace_put(namespace, create_namespace=payload)

    def create_job(self, job_name, location, input_dataset_urns, output_dataset_urns, description):
        if self.namespace is None:
            raise("You have to set the namespace first.")  # TODO change this to be a WARN

        payload = CreateJob(input_dataset_urns, output_dataset_urns, location, description)
        return self.jobs_api_client.namespaces_namespace_jobs_job_put(self.namespace, job_name, create_job=payload)


    def create_job_run(self, job_name, job_run_args, nominal_start_time, nominal_end_time):
        if self.namespace is None:
            raise("You have to set the namespace first.")  # TODO change this to be a WARN

        payload = CreateJobRun(nominal_start_time, nominal_end_time, job_run_args)
        return self.jobs_api_client.namespaces_namespace_jobs_job_runs_post(
            self.namespace, job_name, create_job_run=payload).run_id

    def set_jobrun_state(self, run_id, state):
        if state == RunState.RUNNING:
            self.jobs_api_client.jobs_runs_id_run_put(run_id)
        elif state == RunState.COMPLETED:
            self.jobs_api_client.jobs_runs_id_complete_put(run_id)
        elif state == RunState.FAILED:
            self.jobs_api_client.jobs_runs_id_fail_put(run_id)
        elif state == RunState.ABORTED:
            self.jobs_api_client.jobs_runs_id_abort_put(run_id)
        else:
            # TODO WARN invalid state
            pass
