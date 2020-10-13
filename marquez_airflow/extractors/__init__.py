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

from abc import ABC, abstractmethod

from airflow.models import BaseOperator

from marquez_client.models import DatasetType

log = logging.getLogger(__name__)


class Source:
    name = None
    connection_url = None
    type = None

    def __init__(self, name, type, connection_url):
        self.name = name
        self.type = type
        self.connection_url = connection_url

    def __eq__(self, other):
        return self.name == other.name and \
               self.type == other.type and \
               self.connection_url == other.connection_url

    def __repr__(self):
        return f"Source({self.name!r}, {self.type!r}, {self.connection_url!r})"


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

    @staticmethod
    def from_table(source, table):
        return Dataset(
            type=DatasetType.DB_TABLE,
            name=table,
            source=source
        )

    def __eq__(self, other):
        return self.source == other.source and \
               self.name == other.name and \
               self.type == other.type and \
               self.description == other.description

    def __repr__(self):
        return f"""
            Dataset(\
              {self.source!r}, \
              {self.name!r}, \
              {self.type!r}, \
              {self.description!r})
        """


class StepMetadata:
    # TODO: Define a common way across extractors to build the
    # job name for an operator
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


class BaseExtractor(ABC):
    operator: BaseOperator = None
    operator_class = None

    def __init__(self, operator):
        log.debug("BaseExtractor.init")
        self.operator = operator

    @classmethod
    def get_operator_class(cls):
        return cls.operator_class

    def validate(self):
        # TODO: maybe we should also enforce the module
        assert (self.operator_class is not None and
                self.operator.__class__ == self.operator_class)

    # TODO: Only return a single StepMetadata object.
    @abstractmethod
    def extract(self) -> [StepMetadata]:
        pass

    def extract_on_complete(self, task_instance) -> [StepMetadata]:
        # TODO: This method allows for the partial updating of task
        # metadata on completion. Marquez currently doesn't support
        # partial updates within the context of a DAG run, but this feature
        # will soon be supported:
        # https://github.com/MarquezProject/marquez/issues/816
        #
        # Also, we'll want to revisit the metadata extraction flow,
        # but for now, return an empty set as the default behavior
        # as not all extractors need to handle partial metadata updates.
        return []
