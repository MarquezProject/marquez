from airflow.hooks.mysql_hook import MySqlHook
from airflow.operators.mysql_operator import MySqlOperator

from marquez_airflow.extractors.postgres_extractor import PostgresExtractor


class MySqlExtractor(PostgresExtractor):
    operator_class = MySqlOperator
    source_type = 'MYSQL'

    def __init__(self, operator):
        super().__init__(operator)

    def _information_schema_query(self, table_names: str) -> str:
        return f"""
        SELECT table_schema,
               table_name,
               column_name,
               ordinal_position,
               data_type
          FROM {self.operator.database}.information_schema.columns
         WHERE table_name IN ({table_names});
        """

    def _get_hook(self):
        return MySqlHook(
            mysql_conn_id=self.operator.mysql_conn_id,
            schema=self.operator.database
        )

    def _conn_id(self):
        return self.operator.mysql_conn_id
