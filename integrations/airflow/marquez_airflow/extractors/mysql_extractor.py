import logging

from airflow.hooks.mysql_hook import MySqlHook

from marquez_airflow.extractors.postgres_extractor import PostgresExtractor

log = logging.getLogger(__file__)


# MySql is optional dependency.
def try_load_operator():
    try:
        from airflow.operators.mysql_operator import MySqlOperator
        return MySqlOperator
    except Exception:
        log.warn('Did not find mysql_operator library or failed to import it')
        return None


Operator = try_load_operator()


class MySqlExtractor(PostgresExtractor):
    operator_class = Operator
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
