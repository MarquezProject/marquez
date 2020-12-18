# Datasets

To begin collecting metadata for a dataset, you must first become familiar with how datasets are named. In Marquez, datasets have both a **logical** and **physical** name. The logical name is how your dataset is known to Marquez, while the physical name is how your dataset is known to your source. The logical name for a dataset will contain the following (`.` delimited):

`<source_name>.<dataset_name>`

A logical dataset name **must**:

* Be unique within namespaces
* Be unique within a source
* Contain up to 1,024 characters in length
* Contain only letters (`a-z`, `A-Z`), numbers (`0-9`), or underscores (`_`)

While the physical dataset name (and uniqueness) depends on the source:

| **Source**     | **Fully-Qualifyed Name**                                                                                      |
|----------------|---------------------------------------------------------------------------------------------------------------|
| **PostgreSQL** | [`<database_name>.<schema_name>.<table_name>`](https://www.postgresql.org/docs/current/ddl-schemas.html)      |
| **MySQL**      | [`<database_name>.<table_name>`](https://dev.mysql.com/doc/refman/8.0/en/identifier-qualifiers.html)          |
| **Snowflake**  | [`<database_name>.<schema_name>.<object_name>`](https://docs.snowflake.com/en/sql-reference/identifiers.html) |

## Dataset Name Resolution

Each dataset must be associated with a **source**. A source is the physical location of a dataset, such as a table in a database, or a file on cloud storage. A source enables the logical grouping and mapping of physical datasets to their physical source. 

Therefore, given a namespace and logical dataset name, we can resolve the physical name and location of a dataset. For example, let's say we have the logical name `my-source.my-dataset` under the namespace `my-namespace` associated with the source `my-source`. Using the [DatasetAPI](https://marquezproject.github.io/marquez/openapi.html#tag/Datasets), the physical name would resolve to `public.mytable` with `jdbc:postgresql://localhost:5431/mydb` as the physical source of the datasets. 