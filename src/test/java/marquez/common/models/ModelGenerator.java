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

package marquez.common.models;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import marquez.Generator;

public final class ModelGenerator extends Generator {
  private ModelGenerator() {}

  public static NamespaceName newNamespaceName() {
    return NamespaceName.of("test_namespace" + newId());
  }

  public static OwnerName newOwnerName() {
    return OwnerName.of("test_owner" + newId());
  }

  public static SourceType newSourceType() {
    return SourceType.values()[newIdWithBound(SourceType.values().length - 1)];
  }

  public static SourceName newSourceName() {
    return SourceName.of("test_datasource" + newId());
  }

  public static URI newConnectionUrl() {
    return newConnectionUrlFor(SourceType.POSTGRESQL);
  }

  public static URI newConnectionUrlFor(SourceType type) {
    String connectionUrlString;
    switch (type) {
      case MYSQL:
        connectionUrlString = "jdbc:mysql://localhost:3306/test" + newId();
      case POSTGRESQL:
        connectionUrlString = "jdbc:postgresql://localhost:5432/test" + newId();
      case REDSHIFT:
        connectionUrlString = "jdbc:snowflake://we.snowflakecomputing.com/?db=test" + newId();
      case KAFKA:
        connectionUrlString = "http://localhost:9092";
      default:
        connectionUrlString = "http://localhost:5000";
    }
    return URI.create(connectionUrlString);
  }

  public static List<DatasetName> newDatasetNames(final int limit) {
    return Stream.generate(() -> newDatasetName()).limit(limit).collect(toImmutableList());
  }

  public static DatasetName newDatasetName() {
    return DatasetName.of("test_dataset" + newId());
  }

  public static JobName newJobName() {
    return JobName.of("test_job" + newId());
  }

  public static JobType newJobType() {
    return JobType.values()[newIdWithBound(JobType.values().length - 1)];
  }

  public static URL newLocation() {
    return Utils.toUrl("https://github.com/repo/test/commit/" + newId());
  }

  public static UUID newRunId() {
    return UUID.randomUUID();
  }

  public static String newDescription() {
    return "test_description" + newId();
  }
}
