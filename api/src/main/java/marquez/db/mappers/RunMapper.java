/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.stream.Collectors.toList;
import static marquez.common.models.RunState.NEW;
import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrNull;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;
import static marquez.db.mappers.MapperUtils.toFacetsOrNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.InputDatasetVersion;
import marquez.common.models.NamespaceName;
import marquez.common.models.OutputDatasetVersion;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.db.Columns;
import marquez.service.models.Run;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

@Slf4j
public final class RunMapper implements RowMapper<Run> {
  private final String columnPrefix;

  private static final ObjectMapper MAPPER = Utils.getMapper();

  public RunMapper() {
    this("");
  }

  public RunMapper(String columnPrefix) {
    this.columnPrefix = columnPrefix;
  }

  @Override
  public Run map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    Set<String> columnNames = MapperUtils.getColumnNames(results.getMetaData());
    Optional<Instant> startedAt =
        Optional.ofNullable(timestampOrNull(results, columnPrefix + Columns.STARTED_AT));
    Optional<Long> durationMs =
        Optional.ofNullable(timestampOrNull(results, columnPrefix + Columns.ENDED_AT))
            .flatMap(endedAt -> startedAt.map(s -> s.until(endedAt, MILLIS)));
    List<QueryDatasetVersion> inputDatasetVersions =
        columnNames.contains(columnPrefix + Columns.INPUT_VERSIONS)
            ? toQueryDatasetVersion(results, columnPrefix + Columns.INPUT_VERSIONS)
            : ImmutableList.of();
    List<QueryDatasetVersion> outputDatasetVersions =
        columnNames.contains(columnPrefix + Columns.OUTPUT_VERSIONS)
            ? toQueryDatasetVersion(results, columnPrefix + Columns.OUTPUT_VERSIONS)
            : ImmutableList.of();
    return new Run(
        RunId.of(uuidOrThrow(results, columnPrefix + Columns.ROW_UUID)),
        timestampOrThrow(results, columnPrefix + Columns.CREATED_AT),
        timestampOrThrow(results, columnPrefix + Columns.UPDATED_AT),
        timestampOrNull(results, columnPrefix + Columns.NOMINAL_START_TIME),
        timestampOrNull(results, columnPrefix + Columns.NOMINAL_END_TIME),
        stringOrNull(results, columnPrefix + Columns.CURRENT_RUN_STATE) == null
            ? NEW
            : RunState.valueOf(stringOrNull(results, columnPrefix + Columns.CURRENT_RUN_STATE)),
        columnNames.contains(columnPrefix + Columns.STARTED_AT)
            ? timestampOrNull(results, columnPrefix + Columns.STARTED_AT)
            : null,
        columnNames.contains(columnPrefix + Columns.ENDED_AT)
            ? timestampOrNull(results, columnPrefix + Columns.ENDED_AT)
            : null,
        durationMs.orElse(null),
        toArgsOrNull(results, columnPrefix + Columns.ARGS),
        stringOrThrow(results, columnPrefix + Columns.NAMESPACE_NAME),
        stringOrThrow(results, columnPrefix + Columns.JOB_NAME),
        uuidOrNull(results, columnPrefix + Columns.JOB_VERSION),
        stringOrNull(results, columnPrefix + Columns.LOCATION),
        toInputDatasetVersions(results, inputDatasetVersions, true),
        toOutputDatasetVersions(results, outputDatasetVersions, false),
        toFacetsOrNull(results, columnPrefix + Columns.FACETS));
  }

  private List<QueryDatasetVersion> toQueryDatasetVersion(ResultSet rs, String column)
      throws SQLException {
    String dsString = rs.getString(column);
    if (dsString == null) {
      return Collections.emptyList();
    }
    return Utils.fromJson(dsString, new TypeReference<List<QueryDatasetVersion>>() {});
  }

  private Map<String, String> toArgsOrNull(ResultSet results, String argsColumn)
      throws SQLException {
    if (!Columns.exists(results, argsColumn)) {
      return ImmutableMap.of();
    }
    String args = stringOrNull(results, argsColumn);
    if (args == null) {
      return null;
    }
    return Utils.fromJson(args, new TypeReference<Map<String, String>>() {});
  }

  private List<InputDatasetVersion> toInputDatasetVersions(
      ResultSet rs, List<QueryDatasetVersion> datasetVersionIds, boolean input)
      throws SQLException {
    ImmutableList<QueryDatasetFacet> queryFacets = getQueryDatasetFacets(rs);
    try {
      return datasetVersionIds.stream()
          .map(
              version ->
                  new InputDatasetVersion(
                      version.toDatasetVersionId(), getFacetsMap(input, queryFacets, version)))
          .collect(toList());
    } catch (IllegalStateException e) {
      return Collections.emptyList();
    }
  }

  private List<OutputDatasetVersion> toOutputDatasetVersions(
      ResultSet rs, List<QueryDatasetVersion> datasetVersionIds, boolean input)
      throws SQLException {
    ImmutableList<QueryDatasetFacet> queryFacets = getQueryDatasetFacets(rs);
    try {
      return datasetVersionIds.stream()
          .map(
              version ->
                  new OutputDatasetVersion(
                      version.toDatasetVersionId(), getFacetsMap(input, queryFacets, version)))
          .collect(toList());
    } catch (IllegalStateException e) {
      return Collections.emptyList();
    }
  }

  private ImmutableMap<String, Object> getFacetsMap(
      boolean input,
      ImmutableList<QueryDatasetFacet> queryDatasetFacets,
      QueryDatasetVersion queryDatasetVersion) {
    return ImmutableMap.copyOf(
        queryDatasetFacets.stream()
            .filter(rf -> rf.type.equalsIgnoreCase(input ? "input" : "output"))
            .filter(rf -> rf.datasetVersionUUID.equals(queryDatasetVersion.datasetVersionUUID))
            .collect(
                Collectors.toMap(
                    QueryDatasetFacet::name,
                    facet ->
                        Utils.getMapper()
                            .convertValue(
                                Utils.getMapper().valueToTree(facet.facet).get(facet.name),
                                Object.class),
                    (a1, a2) -> a2 // in case of duplicates, choose more recent
                    )));
  }

  private ImmutableList<QueryDatasetFacet> getQueryDatasetFacets(ResultSet resultSet)
      throws SQLException {
    String column = columnPrefix + Columns.DATASET_FACETS;
    ImmutableList<QueryDatasetFacet> queryDatasetFacets = ImmutableList.of();
    if (Columns.exists(resultSet, column) && resultSet.getObject(column) != null) {
      try {
        queryDatasetFacets =
            MAPPER.readValue(
                ((PGobject) resultSet.getObject(column)).getValue(),
                new TypeReference<ImmutableList<QueryDatasetFacet>>() {});
      } catch (JsonProcessingException e) {
        log.error(String.format("Could not read dataset from job row %s", column), e);
      }
    }
    return queryDatasetFacets;
  }

  record QueryDatasetFacet(
      @JsonProperty("dataset_version_uuid") String datasetVersionUUID,
      String name,
      String type,
      Object facet) {}

  record QueryDatasetVersion(
      String namespace,
      String name,
      UUID version,
      // field required to merge input versions with input dataset facets
      @JsonProperty("dataset_version_uuid") String datasetVersionUUID) {
    public DatasetVersionId toDatasetVersionId() {
      return DatasetVersionId.builder()
          .name(DatasetName.of(name))
          .namespace(NamespaceName.of(namespace))
          .version(version)
          .build();
    }
  }
}
