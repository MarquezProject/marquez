/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringArrayOrThrow;
import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.urlOrNull;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.mappers.MapperUtils.toFacetsOrNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.TagName;
import marquez.db.Columns;
import marquez.service.models.Job;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

@Slf4j
public final class JobMapper implements RowMapper<Job> {
  private static final ObjectMapper MAPPER = Utils.getMapper();
  private static final String JOB_TYPE_FACET_NAME = "jobType";

  @Override
  public Job map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    ImmutableMap<String, Object> facetsOrNull = toFacetsOrNull(results, Columns.FACETS);
    Job job =
        new Job(
            new JobId(
                NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
                JobName.of(stringOrThrow(results, Columns.NAME))),
            JobType.valueOf(
                stringOrThrow(results, Columns.TYPE)), // TODO: store job type in a table
            JobName.of(stringOrThrow(results, Columns.NAME)),
            stringOrThrow(results, Columns.SIMPLE_NAME),
            stringOrNull(results, Columns.PARENT_JOB_NAME),
            uuidOrNull(results, Columns.PARENT_JOB_UUID),
            timestampOrThrow(results, Columns.CREATED_AT),
            timestampOrThrow(results, Columns.UPDATED_AT),
            getDatasetFromJsonOrNull(results, "current_inputs"),
            new HashSet<>(),
            urlOrNull(results, "current_location"),
            stringOrNull(results, Columns.DESCRIPTION),
            // Latest Run is resolved in the JobDao. This can be brought in via a join and
            //  and a jsonb but custom deserializers will need to be introduced
            null,
            null,
            facetsOrNull,
            uuidOrNull(results, Columns.CURRENT_VERSION_UUID),
            getLabels(facetsOrNull),
            toTags(results, "tags"));
    return job;
  }

  Set<DatasetId> getDatasetFromJsonOrNull(@NonNull ResultSet results, String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return new HashSet<>();
    }
    PGobject pgObject = (PGobject) results.getObject(column);
    try {
      Set<DatasetId> datasets = MAPPER.readValue(pgObject.getValue(), new TypeReference<>() {});
      if (datasets == null) {
        return new HashSet<>();
      }
      return datasets;
    } catch (JsonProcessingException e) {
      log.error(String.format("Could not read dataset from job row %s", column), e);
      return new HashSet<>();
    }
  }

  private ImmutableList<String> getLabels(ImmutableMap<String, Object> facetsOrNull) {
    Builder<String> builder = ImmutableList.builder();

    if (facetsOrNull == null) {
      return builder.build();
    }

    Optional.ofNullable(getJobTypeFacetField(facetsOrNull, "jobType"))
        .ifPresent(e -> builder.add(e));

    Optional.ofNullable(getJobTypeFacetField(facetsOrNull, "integration"))
        .ifPresent(e -> builder.add(e));

    return builder.build();
  }

  public static ImmutableSet<TagName> toTags(@NonNull ResultSet results, String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return null;
    }
    List<String> arr = stringArrayOrThrow(results, column);
    return arr.stream().map(TagName::of).collect(ImmutableSet.toImmutableSet());
  }

  private String getJobTypeFacetField(ImmutableMap<String, Object> facetsOrNull, String field) {
    return Optional.ofNullable(facetsOrNull.get(JOB_TYPE_FACET_NAME))
        .filter(o -> o instanceof Map)
        .map(m -> (Map) m)
        .filter(m -> m.containsKey(field))
        .map(m -> m.get(field).toString())
        .orElse(null);
  }
}
