{% macro make_date_partitioned_table(model, relation, dates, should_create, verbose=False) %}

  {% if should_create %}
      {{ adapter.make_date_partitioned_table(relation) }}
  {% endif %}

  {% for date in dates %}
    {% set date = (date | string) %}
    {% if verbose %}
        {% set table_start_time = modules.datetime.datetime.now().strftime("%H:%M:%S") %}
        {{ log(table_start_time ~ ' | -> Running for day ' ~ date, info=True) }}
    {% endif %}

    {% set fixed_sql = model['compiled_sql'] | replace('[DBT__PARTITION_DATE]', date) %}
    {% set _ = adapter.execute_model(model, 'table', fixed_sql, decorator=date) %}
  {% endfor %}

  {% set num_days = dates | length %}
  {% if num_days == 1 %}
      {% set result_str = 'CREATED 1 PARTITION' %}
  {% else %}
      {% set result_str = 'CREATED ' ~ num_days ~ ' PARTITIONS' %}
  {% endif %}

  {{ store_result('main', response=result_str) }}

{% endmacro %}

{% materialization table, adapter='openlineage_bigquery' -%}

  {%- set identifier = model['alias'] -%}
  {%- set old_relation = adapter.get_relation(database=database, schema=schema, identifier=identifier) -%}
  {%- set exists_not_as_table = (old_relation is not none and not old_relation.is_table) -%}
  {%- set target_relation = api.Relation.create(database=database, schema=schema, identifier=identifier, type='table') -%}
  {%- set verbose = config.get('verbose', False) -%}

  {# partitions: iterate over each partition, running a separate query in a for-loop #}
  {%- set partitions = config.get('partitions') -%}

  {% if partitions %}
      {% if partitions is number or partitions is string %}
        {% set partitions = [(partitions | string)] %}
      {% endif %}

      {% if partitions is not iterable %}
        {{ exceptions.raise_compiler_error("Provided `partitions` configuration is not a list. Got: " ~ partitions, model) }}
      {% endif %}
  {% endif %}

  {{ run_hooks(pre_hooks) }}

  {#
      Since dbt uses WRITE_TRUNCATE mode for tables, we only need to drop this thing
      if it is not a table. If it _is_ already a table, then we can overwrite it without downtime
  #}
  {%- if exists_not_as_table -%}
      {{ adapter.drop_relation(old_relation) }}
  {%- endif -%}

  -- emit OpenLineage START event, with all dataset data
  {%- set openlineage_run_id = adapter.emit_start(model, run_started_at.isoformat()) -%}

  -- build model
  {% if partitions %}
    {# Create the dp-table if 1. it does not exist or 2. it existed, but we just dropped it #}
    {%- set should_create = (old_relation is none or exists_not_as_table) -%}
    {{ make_date_partitioned_table(model, target_relation, partitions, should_create, verbose) }}
  {% else %}
    {%- set raw_partition_by = config.get('partition_by', none) -%}
    {%- set partition_by = adapter.parse_partition_by(raw_partition_by) -%}
    {%- set cluster_by = config.get('cluster_by', none) -%}
    {% if not adapter.is_replaceable(old_relation, partition_by, cluster_by) %}
      {% do log("Hard refreshing " ~ old_relation ~ " because it is not replaceable") %}
      {% do adapter.drop_relation(old_relation) %}
    {% endif %}
    {% call statement('main') -%}
      {{ create_table_as(False, target_relation, sql) }}
    {% endcall -%}
  {% endif %}

  {{ run_hooks(post_hooks) }}

  -- emit OpenLineage START event, with all dataset data
  {%- set _ = adapter.emit_complete(openlineage_run_id) -%}

  {% do persist_docs(target_relation, model) %}

  {{ return({'relations': [target_relation]}) }}

{% endmaterialization %}
