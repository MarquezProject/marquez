
class MockIdMapping:

    mapping = None

    def __init__(self):
        self.mapping = {}

    def set(self, key, value):
        self.mapping[key] = value

    def pop(self, key, _session):
        return self.mapping.pop(key)
