
{% macro dbt_bigquery_validate_get_incremental_strategy(config) %}
  {#-- Find and validate the incremental strategy #}
  {%- set strategy = config.get("incremental_strategy", default="merge") -%}

  {% set invalid_strategy_msg -%}
    Invalid incremental strategy provided: {{ strategy }}
    Expected one of: 'merge', 'insert_overwrite'
  {%- endset %}
  {% if strategy not in ['merge', 'insert_overwrite'] %}
    {% do exceptions.raise_compiler_error(invalid_strategy_msg) %}
  {% endif %}

  {% do return(strategy) %}
{% endmacro %}


{% macro bq_insert_overwrite(tmp_relation, target_relation, sql, unique_key, partition_by, partitions, dest_columns) %}

  {% if partitions is not none and partitions != [] %} {# static #}

      {% set predicate -%}
          {{ partition_by.render(alias='DBT_INTERNAL_DEST') }} in (
              {{ partitions | join (', ') }}
          )
      {%- endset %}

      {%- set source_sql -%}
        (
          {{sql}}
        )
      {%- endset -%}

      {{ get_insert_overwrite_merge_sql(target_relation, source_sql, dest_columns, [predicate], include_sql_header=true) }}

  {% else %} {# dynamic #}

      {% set predicate -%}
          {{ partition_by.render(alias='DBT_INTERNAL_DEST') }} in unnest(dbt_partitions_for_replacement)
      {%- endset %}

      {%- set source_sql -%}
      (
        select * from {{ tmp_relation }}
      )
      {%- endset -%}

      -- generated script to merge partitions into {{ target_relation }}
      declare dbt_partitions_for_replacement array<{{ partition_by.data_type }}>;
      declare _dbt_max_partition {{ partition_by.data_type }} default (
          select max({{ partition_by.field }}) from {{ this }}
          where {{ partition_by.field }} is not null
      );

      -- 1. create a temp table
      {{ create_table_as(True, tmp_relation, sql) }}

      -- 2. define partitions to update
      set (dbt_partitions_for_replacement) = (
          select as struct
              array_agg(distinct {{ partition_by.render() }})
          from {{ tmp_relation }}
      );

      {#
        TODO: include_sql_header is a hack; consider a better approach that includes
              the sql_header at the materialization-level instead
      #}
      -- 3. run the merge statement
      {{ get_insert_overwrite_merge_sql(target_relation, source_sql, dest_columns, [predicate], include_sql_header=false) }};

      -- 4. clean up the temp table
      drop table if exists {{ tmp_relation }}

  {% endif %}

{% endmacro %}


{% materialization incremental, adapter='openlineage_bigquery' -%}

  {%- set unique_key = config.get('unique_key') -%}
  {%- set full_refresh_mode = (should_full_refresh()) -%}

  {%- set target_relation = this %}
  {%- set existing_relation = load_relation(this) %}
  {%- set tmp_relation = make_temp_relation(this) %}

  {#-- Validate early so we don't run SQL if the strategy is invalid --#}
  {% set strategy = dbt_bigquery_validate_get_incremental_strategy(config) -%}

  {%- set raw_partition_by = config.get('partition_by', none) -%}
  {%- set partition_by = adapter.parse_partition_by(raw_partition_by) -%}
  {%- set partitions = config.get('partitions', none) -%}
  {%- set cluster_by = config.get('cluster_by', none) -%}

  {{ run_hooks(pre_hooks) }}

  {% if existing_relation is none %}
      {% set build_sql = create_table_as(False, target_relation, sql) %}
  {% elif existing_relation.is_view %}
      {#-- There's no way to atomically replace a view with a table on BQ --#}
      {{ adapter.drop_relation(existing_relation) }}
      {% set build_sql = create_table_as(False, target_relation, sql) %}
  {% elif full_refresh_mode %}
      {#-- If the partition/cluster config has changed, then we must drop and recreate --#}
      {% if not adapter.is_replaceable(existing_relation, partition_by, cluster_by) %}
          {% do log("Hard refreshing " ~ existing_relation ~ " because it is not replaceable") %}
          {{ adapter.drop_relation(existing_relation) }}
      {% endif %}
      {% set build_sql = create_table_as(False, target_relation, sql) %}
  {% else %}
     {% set dest_columns = adapter.get_columns_in_relation(existing_relation) %}

     {#-- if partitioned, use BQ scripting to get the range of partition values to be updated --#}
     {% if strategy == 'insert_overwrite' %}

        {% set missing_partition_msg -%}
          The 'insert_overwrite' strategy requires the `partition_by` config.
        {%- endset %}
        {% if partition_by is none %}
          {% do exceptions.raise_compiler_error(missing_partition_msg) %}
        {% endif %}

        {% set build_sql = bq_insert_overwrite(
            tmp_relation,
            target_relation,
            sql,
            unique_key,
            partition_by,
            partitions,
            dest_columns) %}

     {% else %}
       {#-- wrap sql in parens to make it a subquery --#}
       {%- set source_sql -%}
         (
           {{sql}}
         )
       {%- endset -%}

       {% set build_sql = get_merge_sql(target_relation, source_sql, unique_key, dest_columns) %}

     {% endif %}

  {% endif %}

  {%- call statement('main') -%}
    {{ build_sql }}
  {% endcall %}

  {{ run_hooks(post_hooks) }}

  {% set target_relation = this.incorporate(type='table') %}

  {% do persist_docs(target_relation, model) %}

  {{ return({'relations': [target_relation]}) }}

{%- endmaterialization %}
