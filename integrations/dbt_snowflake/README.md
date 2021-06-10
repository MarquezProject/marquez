# dbt-snowflake-openlineage

Plugin for [dbt](https://github.com/fishtown-analytics/dbt) (data build tool) for automatic metadata and lineage collection.


## Requirements

```
Python 3.6+
sqlparse>=0.3.1
```

## Installation

`pip install dbt-openlineage-snowflake`


## Configuration

You need to configure dbt's [profiles.yaml](https://docs.getdbt.com/dbt-cli/configure-your-profile) file.
To use this plugin you need to use type: openlineage_snowflake.

It handles the same configuration flags as original snowflake plugin, with the addition of OpenLineage related ones:

```bash
    openlineage_url - sets URL of OpenLineage server where events will be pushed
    openlineage_timeout - optional - sets timeout for connecting to OpenLineage server. By default it's set to 5 seconds.
    openlineage_apikey - optional - sets api key for OpenLineage server
```
