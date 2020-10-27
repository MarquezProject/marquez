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
import subprocess

import airflow
from airflow.models import Connection
from airflow.utils.db import provide_session
from airflow.version import version as airflow_version

from marquez_airflow.version import VERSION as MARQUEZ_AIRFLOW_VERSION

log = logging.getLogger(__name__)


class JobIdMapping:
    _instance = None

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super(
                JobIdMapping, cls).__new__(cls, *args, **kwargs)
        return cls._instance

    @staticmethod
    def set(key, val):
        airflow.models.Variable.set(key, val)

    @staticmethod
    def pop(key, session):
        return JobIdMapping.get(key=key, session=session, delete=True)

    @staticmethod
    def get(key, session, delete=False):
        if session:
            q = session.query(airflow.models.Variable).filter(
                airflow.models.Variable.key == key)
            if not q.first():
                return
            else:
                val = q.first().val
                if delete:
                    q.delete(synchronize_session=False)
                return val

    @staticmethod
    def make_key(job_name, run_id):
        return "marquez_id_mapping-{}-{}".format(job_name, run_id)


def url_to_https(url):
    base_url = None
    if url.startswith('git@'):
        part = url.split('git@')[1:2]
        if part:
            base_url = f'https://{part[0].replace(":", "/", 1)}'
    elif url.startswith('https://'):
        base_url = url

    if not base_url:
        raise ValueError(f'Unable to extract location from: {url}')

    if base_url.endswith('.git'):
        base_url = base_url[:-4]
    return base_url


def get_location(file_path):
    # move to the file directory
    abs_path = os.path.abspath(file_path)
    file_name = os.path.basename(file_path)
    cwd = os.path.dirname(abs_path)

    # get the repo url
    repo_url = execute_git(cwd, ['config', '--get', 'remote.origin.url'])

    # get the repo relative path
    repo_relative_path = execute_git(cwd, ['rev-parse', '--show-prefix'])

    # get the commitId for the particular file
    commit_id = execute_git(cwd, ['rev-list', 'HEAD', '-1', '--', file_name])

    # build the URL
    base_url = url_to_https(repo_url)
    return f'{base_url}/blob/{commit_id}/{repo_relative_path}{file_name}'


def execute_git(cwd, params):
    p = subprocess.Popen(['git'] + params,
                         cwd=cwd, stdout=subprocess.PIPE, stderr=None)
    p.wait(timeout=0.5)
    out, err = p.communicate()
    return out.decode('utf8').strip()


def get_connection_uri(conn_id):
    """
    Return the connection URI for the given ID. We first attempt to lookup
    the connection URI via AIRFLOW_CONN_<conn_id>, else fallback on querying
    the Airflow's connection table.
    """
    conn_uri = os.environ.get('AIRFLOW_CONN_' + conn_id.upper())
    log.debug(conn_uri)
    return conn_uri or _get_connection(conn_id).get_uri()


@provide_session
def _get_connection(conn_id, session=None):
    # TODO: We may want to throw an exception if the connection
    # does not exist (ex: AirflowConnectionException). The connection
    # URI is required when collecting metadata for a data source.
    return (session
            .query(Connection)
            .filter(Connection.conn_id == conn_id)
            .first())


def get_job_name(task):
    return f'{task.dag_id}.{task.task_id}'


def add_airflow_info_to(task, steps_metadata):
    log.info(f"add_airflow_info_to({task}, {steps_metadata})")

    for step_metadata in steps_metadata:
        # Add operator info
        operator = \
            f'{task.__class__.__module__}.{task.__class__.__name__}'

        step_metadata.context['airflow.operator'] = operator
        step_metadata.context['airflow.task_info'] = str(task.__dict__)

        # Add version info
        step_metadata.context['airflow.version'] = airflow_version
        step_metadata.context['marquez_airflow.version'] = \
            MARQUEZ_AIRFLOW_VERSION

    return steps_metadata
