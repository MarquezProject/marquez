/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.service.models;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import marquez.common.models.SourceQualifier;

@EqualsAndHashCode
@ToString
public final class SourceTypes {
  @Value
  static final class Db {
    String type;
    List<String> dataTypes;

    @JsonCreator
    public Db(@NonNull final String type, @NonNull final List<String> dataTypes) {
      this.type = checkNotBlank(type, "type must not be blank");
      this.dataTypes = dataTypes.stream().map(String::toUpperCase).collect(toImmutableList());
    }

    boolean supports(@NonNull String dataType) {
      checkNotBlank(dataType, "dataType must not be blank");
      return dataTypes.contains(dataType.toUpperCase());
    }
  }

  @Value
  static final class Stream {
    String type;

    @JsonCreator
    public Stream(@NonNull final String type) {
      this.type = checkNotBlank(type, "type must not be blank");
    }
  }

  @Value
  static final class Filesystem {
    String type;

    @JsonCreator
    public Filesystem(@NonNull final String type) {
      this.type = checkNotBlank(type, "type must not be blank");
    }
  }

  private static final boolean NO_SUPPORT = false;

  private Map<String, Db> dbMap;
  private Map<String, Stream> streamMap;
  private Map<String, Filesystem> filesystemMap;

  public SourceTypes() {
    this(ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
  }

  @JsonCreator
  public SourceTypes(
      @Nullable final List<Db> dbs,
      @Nullable final List<Stream> streams,
      @Nullable final List<Filesystem> filesystems) {
    this.dbMap = toDbMap(dbs);
    this.streamMap = toStreamMap(streams);
    this.filesystemMap = toFilesystemMap(filesystems);
  }

  private Map<String, Db> toDbMap(@Nullable final List<Db> overrides) {
    final Map<String, Db> dbMap = Maps.newHashMap();
    DBS.forEach(
        db -> {
          dbMap.put(db.getType().toUpperCase(), db);
        });
    // Overrides
    if (overrides != null) {
      overrides.forEach(
          override -> {
            dbMap.put(override.getType().toUpperCase(), override);
          });
    }
    return ImmutableMap.copyOf(dbMap);
  }

  private Map<String, Stream> toStreamMap(@Nullable final List<Stream> overrides) {
    final Map<String, Stream> streamMap = Maps.newHashMap();
    STREAMS.forEach(
        stream -> {
          streamMap.put(stream.getType().toUpperCase(), stream);
        });
    // Overrides
    if (overrides != null) {
      overrides.forEach(
          override -> {
            streamMap.put(override.getType().toUpperCase(), override);
          });
    }
    return ImmutableMap.copyOf(streamMap);
  }

  private Map<String, Filesystem> toFilesystemMap(@Nullable final List<Filesystem> overrides) {
    final Map<String, Filesystem> filesystemMap = Maps.newHashMap();
    FILESYSTEMS.forEach(
        filesystem -> {
          filesystemMap.put(filesystem.getType().toUpperCase(), filesystem);
        });
    // Overrides
    if (overrides != null) {
      overrides.forEach(
          override -> {
            filesystemMap.put(override.getType().toUpperCase(), override);
          });
    }
    return ImmutableMap.copyOf(filesystemMap);
  }

  public boolean exists(@NonNull String type, @NonNull SourceQualifier qualifier) {
    checkNotBlank(type, "type must not be blank");
    final String typeAsUpperCase = type.toUpperCase();
    switch (qualifier) {
      case DB:
        return dbMap.containsKey(typeAsUpperCase);
      case STREAM:
        return streamMap.containsKey(typeAsUpperCase);
      case FILESYSTEM:
        return filesystemMap.containsKey(typeAsUpperCase);
      default:
        throw new AssertionError();
    }
  }

  public boolean typeHasSupportFor(
      @NonNull String type, @NonNull SourceQualifier qualifier, @NonNull String dataType) {
    checkNotBlank(type, "type must not be blank");
    checkNotBlank(dataType, "dataType must not be blank");
    if (!exists(type, qualifier)) {
      return NO_SUPPORT;
    }

    final String typeAsUpperCase = type.toUpperCase();
    switch (qualifier) {
      case DB:
        return dbMap.containsKey(typeAsUpperCase)
            ? dbMap.get(typeAsUpperCase).supports(dataType)
            : NO_SUPPORT;
      default:
        // We currently only support data types for a database source.
        return NO_SUPPORT;
    }
  }

  private static final Db MYSQL =
      new Db(
          "MySQL",
          ImmutableList.of(
              "INTEGER",
              "INT",
              "SMALLINT",
              "TINYINT",
              "MEDIUMINT",
              "BIGINT",
              "DECIMAL",
              "NUMERIC",
              "FLOAT",
              "REAL",
              "DOUBLE",
              "DOUBLE PRECISION",
              "BOOLEAN",
              "BOOL",
              "BIT",
              "DATE",
              "DATETIME",
              "TIMESTAMP",
              "TIME",
              "YEAR",
              "CHAR",
              "VARCHAR",
              "BINARY",
              "VARBINARY",
              "TINYBLOB",
              "BLOB",
              "MEDIUMBLOB",
              "LONGBLOB",
              "TINYTEXT",
              "TEXT",
              "MEDIUMTEXT",
              "LONGTEXT",
              "ENUM",
              "SET",
              "GEOMETRY",
              "POINT",
              "LINESTRING",
              "POLYGON",
              "MULTIPOINT",
              "MULTILINESTRING",
              "MULTIPOLYGON",
              "GEOMETRYCOLLECTION",
              "JSON"));

  private static final Db POSTGRESQL =
      new Db(
          "PostgreSQL",
          ImmutableList.of(
              "SMALLINT",
              "INTEGER",
              "INT",
              "BIGINT",
              "DECIMAL",
              "NUMERIC",
              "FLOAT",
              "REAL",
              "DOUBLE PRECISION",
              "SMALLSERIAL",
              "SERIAL",
              "BIGSERIAL",
              "MONEY",
              "CHARACTER",
              "CHAR",
              "CHARACTER VARYING",
              "VARCHAR",
              "TEXT",
              "BYTEA",
              "TIME",
              "TIMESTAMP",
              "INTERVAL",
              "BOOLEAN",
              "BOOL",
              "POINT",
              "LINE",
              "LSEG",
              "BOX",
              "PATH",
              "POLYGON",
              "CIRCLE",
              "CIDR",
              "INET",
              "MACADDR",
              "BIT",
              "BIT VARYING",
              "UUID",
              "XML",
              "JSON"));

  private static final Db REDSHIFT =
      new Db(
          "Redshift",
          ImmutableList.of(
              "SMALLINT",
              "INT2",
              "INTEGER",
              "INT",
              "INT4",
              "BIGINT",
              "INT8",
              "DECIMAL",
              "NUMERIC",
              "REAL",
              "FLOAT4",
              "DOUBLE PRECISION",
              "FLOAT8",
              "FLOAT",
              "BOOLEAN",
              "BOOL",
              "CHAR",
              "CHARACTER",
              "NCHAR",
              "BPCHAR",
              "VARCHAR",
              "CHARACTER VARYING",
              "NVARCHAR",
              "TEXT",
              "DATE",
              "TIMESTAMP",
              "TIMESTAMP WITHOUT TIME ZONE",
              "TIMESTAMPTZ",
              "TIMESTAMP WITH TIME ZONE"));

  private static final Db SNOWFLAKE =
      new Db(
          "Snowflake",
          ImmutableList.of(
              "NUMBER",
              "DECIMAL",
              "NUMERIC",
              "INT",
              "INTEGER",
              "BIGINT",
              "SMALLINT",
              "FLOAT",
              "FLOAT4",
              "FLOAT8",
              "DOUBLE",
              "DOUBLE PRECISION",
              "REAL",
              "VARCHAR",
              "CHAR",
              "CHARACTER",
              "STRING",
              "TEXT",
              "BINARY",
              "VARBINARY",
              "BOOLEAN",
              "DATE",
              "DATETIME",
              "TIME",
              "TIMESTAMP",
              "TIMESTAMP_LTZ",
              "TIMESTAMP_NTZ",
              "TIMESTAMP_TZ",
              "VARIANT",
              "OBJECT",
              "ARRAY"));

  private static final List<Db> DBS = ImmutableList.of(MYSQL, POSTGRESQL, REDSHIFT, SNOWFLAKE);

  private static final Stream KAFKA = new Stream("Kafka");
  private static final Stream KINESIS = new Stream("Kinesis");
  private static final List<Stream> STREAMS = ImmutableList.of(KAFKA, KINESIS);

  private static final Filesystem S3 = new Filesystem("S3");
  private static final Filesystem GCS = new Filesystem("GCS");
  private static final List<Filesystem> FILESYSTEMS = ImmutableList.of(S3, GCS);
}
