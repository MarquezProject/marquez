import uuid
from unittest.mock import create_autospec

from marquez_client import MarquezClient


def get_mock_marquez_client():
    uuid_generator = generate_uuid()

    def create_job_run(*args, **kwargs):
        assert len(args) == 1
        return {"runId": str(next(uuid_generator))}

    def create_source(*args, **kwargs):
        assert len(args) >= 2
        name = args[0]
        # type = args[1] # commenting out as not used for now
        # connection_url = args[2]
        # source_type = connection_url.split(':')[1]
        return {'name': name}

    def create_dataset(*args, **kwargs):
        assert len(args) >= 2
        dataset_name = args[0]
        return {'name': dataset_name}

    client = create_autospec(MarquezClient)

    client.create_job_run.side_effect = create_job_run
    client.create_source.side_effect = create_source
    client.create_dataset.side_effect = create_dataset
    return client


def generate_uuid():
    idx = 0
    while True:
        yield uuid.UUID(int=idx)
        idx += 1
