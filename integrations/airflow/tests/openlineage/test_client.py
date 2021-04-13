from unittest.mock import MagicMock

import pytest

from openlineage.client import OpenLineageClient
from openlineage.run import RunEvent, RunState, Run, Job


def test_client_fails_with_wrong_event_type():
    session = MagicMock()
    client = OpenLineageClient(url="http://example.com", session=session)

    with pytest.raises(ValueError):
        client.emit("event")


def test_client_fails_to_create_with_wrong_url():
    session = MagicMock()
    with pytest.raises(ValueError):
        OpenLineageClient(url="notanurl", session=session)
    with pytest.raises(ValueError):
        OpenLineageClient(url="http://", session=session)
    with pytest.raises(ValueError):
        OpenLineageClient(url="example.com", session=session)
    with pytest.raises(ValueError):
        OpenLineageClient(url="http:example.com", session=session)
    with pytest.raises(ValueError):
        OpenLineageClient(url="http:/example.com", session=session)
    with pytest.raises(ValueError):
        OpenLineageClient(url="196.168.0.1", session=session)


def test_client_sends_proper_json_with_minimal_event():
    session = MagicMock()
    client = OpenLineageClient(url="http://example.com", session=session)

    client.emit(
        RunEvent(
            RunState.START,
            "2020-01-01",
            Run("1"),
            Job("openlineage", "job"),
            "producer"
        )
    )

    session.post.assert_called_with(
        "http://example.com/api/v1/lineage",
        '{"eventTime": "2020-01-01", "eventType": "START", "inputs": [], "job": '
        '{"facets": {}, "name": "job", "namespace": "openlineage"}, "outputs": [], '
        '"producer": "producer", "run": {"facets": {}, "runId": "1"}}',
        timeout=5.0,
        verify=True
    )
