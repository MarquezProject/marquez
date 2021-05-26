from typing import Type, Optional
from marquez_airflow.extractors import BaseExtractor
from marquez_airflow.extractors.bigquery_extractor import BigQueryExtractor
from marquez_airflow.extractors.great_expectations_extractor import GreatExpectationsExtractor
from marquez_airflow.extractors.postgres_extractor import PostgresExtractor


_extractors = [
    PostgresExtractor,
    BigQueryExtractor,
    GreatExpectationsExtractor
]

_patchers = [
    GreatExpectationsExtractor
]


class Extractors:
    """
    This exposes implemented extractors, while hiding ones that require additional, unmet
    dependency. Patchers are a category of extractor that needs to hook up to operator's
    internals during DAG creation.
    """
    def __init__(self):
        # Do not expose extractors relying on external dependencies that are not installed
        self.extractors = {
            extractor.operator_class: extractor
            for extractor
            in _extractors
            if hasattr(extractor, 'operator_class')
        }

        self.patchers = {
            extractor.operator_class: extractor
            for extractor
            in _patchers
            if hasattr(extractor, 'operator_class')
        }

    def get_extractor_class(self, clazz: Type) -> Optional[Type[BaseExtractor]]:
        if clazz in self.extractors:
            return self.extractors[clazz]
        return None

    def get_patcher_class(self, clazz: Type) -> Optional[Type[BaseExtractor]]:
        if clazz in self.patchers:
            return self.patchers[clazz]
        return None
