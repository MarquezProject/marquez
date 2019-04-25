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

import airflow


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
        if session:
            q = session.query(airflow.models.Variable).filter(
                airflow.models.Variable.key == key)
            if not q.first():
                return
            else:
                val = q.first().val
                q.delete(synchronize_session=False)
                return val

    @staticmethod
    def make_key(job_name, run_id):
        return "marquez_id_mapping-{}-{}".format(job_name, run_id)
