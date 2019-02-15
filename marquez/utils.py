import airflow
from airflow.utils.db import provide_session


class JobIdMapping:
    _instance = None

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super(JobIdMapping, cls).__new__(cls, *args, **kwargs)
        return cls._instance

    def set(self, key, val):
        airflow.models.Variable.set(key, val)

    @provide_session
    def pop(self, key, session=None):
        q = session.query(airflow.models.Variable).filter(airflow.models.Variable.key == key)
        if not q.first():
            return
        else:
            val = q.first().val
            q.delete(synchronize_session=False)
            return val

    @staticmethod
    def make_key(job_name, run_id):
        return "mqz_id_mapping-{}-{}".format(job_name, run_id)
