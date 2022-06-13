/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.graphql.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.graphql.mapper.LineageResultMapper.JobResult;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

@Slf4j
public class LineageResultMapper implements RowMapper<JobResult> {

  @Override
  public JobResult map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    String namespaceName = results.getObject("namespace", String.class);
    String jobName = results.getObject("name", String.class);
    PGobject inEdges = results.getObject("inEdges", PGobject.class);
    PGobject outEdges = results.getObject("outEdges", PGobject.class);

    return new JobResult(
        namespaceName, jobName, convertToDatasetResult(inEdges), convertToDatasetResult(outEdges));
  }

  private LinkedHashSet<DatasetResult> convertToDatasetResult(PGobject obj) {
    if (obj == null) {
      return new LinkedHashSet<>();
    }

    try {
      return Utils.getMapper()
          .readValue(obj.getValue(), new TypeReference<LinkedHashSet<DatasetResult>>() {});
    } catch (JsonProcessingException e) {
      log.error("Could not read lineage node", e);
      return null;
    }
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  public abstract static class LineageResult {
    String namespace;
    String name;
    LineageType type;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      LineageResult result = (LineageResult) o;
      return namespace.equals(result.namespace) && name.equals(result.name) && type == result.type;
    }

    @Override
    public int hashCode() {
      return Objects.hash(namespace, name, type);
    }
  }

  public enum LineageType {
    JOB,
    DATASET
  }

  @Getter
  @Setter
  public static class JobResult extends LineageResult {
    LinkedHashSet<DatasetResult> inEdges;
    LinkedHashSet<DatasetResult> outEdges;

    public JobResult(
        String namespace,
        String name,
        LinkedHashSet<DatasetResult> inEdges,
        LinkedHashSet<DatasetResult> outEdges) {
      super(namespace, name, LineageType.JOB);
      this.inEdges = inEdges;
      this.outEdges = outEdges;
    }
  }

  @Getter
  @Setter
  public static class DatasetResult extends LineageResult {
    LinkedHashSet<JobResult> inEdges;
    LinkedHashSet<JobResult> outEdges;

    public DatasetResult() {
      type = LineageType.DATASET;
    }
  }
}
