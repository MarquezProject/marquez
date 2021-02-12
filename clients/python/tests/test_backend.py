import pytest

from marquez_client.backend import Backend


def test_put():
    with pytest.raises(NotImplementedError):
        Backend().put(path='', headers='', payload='')


def test_post():
    with pytest.raises(NotImplementedError):
        Backend().post(path='', headers='')
