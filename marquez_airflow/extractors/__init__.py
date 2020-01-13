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

import inspect
import pkgutil
import sys

from airflow.models import BaseOperator


def get_extractors():
    extractors = {}
    for mi in pkgutil.walk_packages(path=__path__,
                                    onerror=lambda x: None,
                                    prefix=__name__+'.'):
        try:
            pkgutil.get_loader(mi.name).load_module()
            for name, cls in inspect.getmembers(sys.modules[mi.name],
                                                inspect.isclass):
                if issubclass(cls, BaseExtractor) and cls != BaseExtractor:
                    extractors[cls.get_operator_class()] = cls
        except Exception:
            pass
    return extractors


class Source:
    name = None
    connection_url = None
    type = None

    def __init__(self, name, type, connection_url):
        self.name = name
        self.type = type
        self.connection_url = connection_url

    def __repr__(self):
        return "{}+{}".format(self.name, self.connection_url)


class Dataset:
    source: Source = None
    name = None
    description = None
    type = None

    def __init__(self, source, name, type, description=None):
        self.source = source
        self.name = name
        self.type = type
        self.description = description

    def __repr__(self):
        return "{}/{}".format(self.source, self.name)


class StepMetadata:
    name = None
    location = None
    inputs = []
    outputs = []
    context = {}

    def __init__(self, name, location=None, inputs=None, outputs=None,
                 context=None):
        self.name = name
        self.location = location
        if inputs:
            self.inputs = inputs
        if outputs:
            self.outputs = outputs
        if context:
            self.context = context

    def __repr__(self):
        return "name: {}\t inputs: {} \t outputs: {}".format(
            self.name,
            ','.join([str(i) for i in self.inputs]),
            ','.join([str(o) for o in self.outputs]))


class BaseExtractor:
    operator: BaseOperator = None
    operator_class = None

    def __init__(self, operator):
        self.operator = operator

    @classmethod
    def get_operator_class(cls):
        return cls.operator_class

    def validate(self):
        # TODO: maybe we should also enforce the module
        assert(self.operator_class is not None and
               self.operator.__class__.__name__ == self.operator_class)

    def extract(self) -> [StepMetadata]:
        raise NotImplementedError
