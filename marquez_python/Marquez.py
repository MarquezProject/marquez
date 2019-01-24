import logging
import os

import requests


class Marquez:
    __instance__ = None

    @staticmethod
    def get_instance(marquez_host="http://localhost", marquez_port=8080):
        if not Marquez.__instance__:
            Marquez(marquez_host, marquez_port)
        else:
            logging.info("Using existing instance of Marquez %s", str(Marquez.__instance__))
        return Marquez.__instance__

    @staticmethod
    def is_enabled():
        # TODO: Set to use real config
        # Do we have access to config data here?
        return True

    def __init__(self, marquez_host="http://localhost", marquez_port=8080):
        if Marquez.__instance__:
            raise Exception("Cannot re-instantiate Marquez")
        self.marquez_host = marquez_host
        self.marquez_port = marquez_port
        self.marquez_host_uri = "{0}:{1}".format(marquez_host, marquez_port)
        Marquez.__instance__ = self

    def register_marquez_job_run(self, ):


    def register_marquez_job_run(self, dag):
        """
        This method will call out to the external system responsible for storing dag
        metadata and create the background context for the dag run
        :return: the id of the run
        """
        os.environ['no_proxy'] = 'localhost'
        print('logging from report new dag metadata')
        if hasattr(dag, 'dag_id'):
            dag_id = dag.dag_id
        else:
            dag_id = "default_dag_id"

        if hasattr(dag, 'filepath'):
            dag_filepath = dag.filepath
        else:
            dag_filepath = "default_dag_filepath"

        if hasattr(dag, 'default_args'):
            default_args = dag.default_args
        else:
            default_args = "default_dag_args"

        if hasattr(dag, 'last_loaded'):
            last_loaded = dag.last_loaded
        else:
            last_loaded = "default_last_loaded"

        ##### SAMPLE VALUES #####
        namespace = "holiday_gift_logistics"
        namespace_owner = "santa"
        namespace_description = "Metadata around jobs and data related to" \
                                "gift logistics (e.g. naughty-and-nice-list.csv)";

        job_name = dag_id
        location = "git://path/to/job/location"
        description = dag_filepath

        # TODO: Get these from the job itself
        sample_input_dataset_urns = ["uri:scheme:input:file1", "uri:scheme:input:file2"]
        sample_output_dataset_urns = ["uri:scheme:output:file3", "uri:scheme:output:file4"]

        job_run_args = {
            "runArgs": json.dumps(default_args, indent=4, sort_keys=True, default=str),
            "nominalStartTime": str(last_loaded)
        }

        self.create_namespace_if_not_exists(namespace, namespace_owner, namespace_description)
        self.create_job_if_not_exists(namespace, job_name, location, sample_input_dataset_urns,
                                      sample_output_dataset_urns, description)
        marquez_job_run_id = self.create_job_run(namespace, job_name, job_run_args)
        logging.info("The job run id is: %s", marquez_job_run_id)
        return marquez_job_run_id

    def update_job_run_state(self, job_run_id, state):
        path = Marquez.compose_job_run_path(job_run_id)
        if state == State.FAILED:
            path += "/fail"
        if state == State.SHUTDOWN:
            path += "/abort"
        elif state == State.RUNNING:
            path += "/run"
        elif state == State.SUCCESS:
            path += "/complete"
        response_code = self._put_request(path).status_code
        if response_code != http.HTTPStatus.OK:
            logging.error("Could not properly update state! Received response code %s", response_code)
        else:
            logging.info("Updated job run %s to state %s!", job_run_id, state)

    def create_namespace_if_not_exists(self, namespace, namespace_owner, namespace_description):
        if not self.does_namespace_exist(namespace):
            self.create_namespace(namespace, namespace_owner, namespace_description)

    def create_job_if_not_exists(self, namespace, job_name, location,
                                 input_dataset_urns, output_dataset_urns, description=None):
        job_args = {
            "inputDatasetUrns": input_dataset_urns,
            "outputDatasetUrns": output_dataset_urns,
            "location": location
        }

        if description:
            job_args['description'] = description

        job_path = self.compose_job_path(namespace, job_name)

        logging.info("job args are: %s", json.dumps(job_args))
        job_creation_response = self._put_request(job_path, job_args)
        if job_creation_response.status_code != http.HTTPStatus.CREATED:
            logging.error("Could not create job {0}:{1}. "
                          "Error was: {2}".format(namespace, job_name, job_creation_response.text))

    def create_namespace(self, namespace, namespace_owner, namespace_description=None):
        create_namespace_args = {
            "owner": namespace_owner
        }

        if namespace_description:
            create_namespace_args['description'] = namespace_description

        path = "api/v1/namespaces/{0}/".format(namespace)

        namespace_response = json.loads(self._put_request(path, create_namespace_args).text)
        logging.info("MARQUEZ: Just created namespace {0}".format(namespace))
        return namespace_response

    def create_job_run(self, namespace, job_name, job_run_args):
        path = "api/v1/namespaces/{0}/jobs/{1}/runs".format(namespace, job_name)

        job_run_response = json.loads(self._post_request(path, job_run_args).text)
        logging.info("MARQUEZ: Just created a job run for job {0}:{1}".format(namespace, job_name))
        return job_run_response['runId']

    def does_namespace_exist(self, namespace):
        get_namespace_path = "api/v1/namespaces/{0}".format(namespace)
        get_namespace_response = self._get_request(get_namespace_path)

        if get_namespace_response.status_code == http.HTTPStatus.OK:
            logging.info("Namespace %s indeed exists", namespace)
            return True
        logging.info("Namespace %s does not exist! It must be created", namespace)
        return False

    @staticmethod
    def compose_job_path(namespace, job_name):
        return "api/v1/namespaces/{0}/jobs/{1}".format(namespace, job_name)

    @staticmethod
    def compose_job_run_path(job_run_id):
        return "api/v1/jobs/runs/{0}".format(job_run_id)

    def _put_request(self, path, args=None):
        full_path = "{0}/{1}".format(self.marquez_host_uri, path)
        os.environ['no_proxy'] = 'localhost'
        logging.info("Full path is: %s", full_path)
        if args:
            logging.info("args are: " + json.dumps(args))
        return requests.put(url=full_path, json=args)

    def _post_request(self, path, args=None):
        full_path = "{0}/{1}".format(self.marquez_host_uri, path)
        logging.info("full path is: %s", full_path)
        logging.info("args are : %s", json.dumps(args))
        return requests.post(url=full_path, json=args)

    def _get_request(self, path, args=None):
        full_path = "{0}/{1}".format(self.marquez_host_uri, path)
        if args:
            kv = args.popitem()
            key = kv[0]
            value = kv[1]

            arg_str = "{0}={1}".format(key, value)
            while args:
                kv = args.popitem()
                key = kv[0]
                value = kv[1]
                arg_str += "&{0}={1}".format(key, value)
                full_path = "{0}?{1}".format(full_path, arg_str)
        return requests.get(full_path)