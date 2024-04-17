/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import marquez.common.models.JobType;

/**
 * Requires jackson serialization features: mapper.registerModule(new JavaTimeModule());
 * mapper.setSerializationInclusion(Include.NON_NULL);
 * mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
 * mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Valid
@ToString
public class LineageEvent extends BaseEvent {

  private String eventType;

  @NotNull private ZonedDateTime eventTime;
  @Valid @NotNull private LineageEvent.Run run;
  @Valid @NotNull private LineageEvent.Job job;
  @Valid private List<Dataset> inputs;
  @Valid private List<Dataset> outputs;
  @Valid @NotNull private String producer;
  @Valid private URI schemaURL;

  @JsonIgnore
  public boolean isTerminalEvent() {
    return (eventType != null)
        && (eventType.equalsIgnoreCase("COMPLETE") || eventType.equalsIgnoreCase("FAIL"));
  }

  @JsonIgnore
  public boolean isTerminalEventForStreamingJobWithNoDatasets() {
    return isTerminalEvent()
        && (job != null && job.isStreamingJob())
        && (outputs == null || outputs.isEmpty())
        && (inputs == null || inputs.isEmpty());
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  @NotNull
  public static class Run extends BaseJsonModel {

    @NotNull private String runId;
    @Valid private RunFacet facets;
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Valid
  @ToString
  @JsonPropertyOrder({"nominalTime", "parent"})
  public static class RunFacet {

    @Valid private NominalTimeRunFacet nominalTime;

    @JsonAlias(
        "parentRun") // the Airflow integration previously reported parentRun instead of parent
    @Valid
    private ParentRunFacet parent;

    @Builder.Default @JsonIgnore private Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    public void setFacet(String key, Object value) {
      additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFacets() {
      return additional;
    }

    public NominalTimeRunFacet getNominalTime() {
      return nominalTime;
    }

    public ParentRunFacet getParent() {
      return parent;
    }
  }

  @Getter
  @Setter
  private abstract static class BaseFacet {
    @NotNull private URI _producer;
    @NotNull private URI _schemaURL;
    @JsonIgnore private Map<String, Object> additional = new LinkedHashMap<>();

    protected BaseFacet() {}

    public BaseFacet(@NotNull URI _producer, @NotNull URI _schemaURL) {
      this._producer = _producer;
      this._schemaURL = _schemaURL;
    }

    @JsonAnySetter
    public void setFacet(String key, Object value) {
      additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFacets() {
      return additional;
    }
  }

  @Getter
  @Setter
  @Valid
  @ToString
  @NoArgsConstructor
  public static class NominalTimeRunFacet extends BaseFacet {

    @NotNull private ZonedDateTime nominalStartTime;
    private ZonedDateTime nominalEndTime;

    @Builder
    public NominalTimeRunFacet(
        @NotNull URI _producer,
        @NotNull URI _schemaURL,
        @NotNull ZonedDateTime nominalStartTime,
        ZonedDateTime nominalEndTime) {
      super(_producer, _schemaURL);
      this.nominalStartTime = nominalStartTime;
      this.nominalEndTime = nominalEndTime;
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  public static class ParentRunFacet extends BaseFacet {

    @Valid @NotNull private RunLink run;
    @Valid @NotNull private JobLink job;

    @Builder
    public ParentRunFacet(
        @NotNull URI _producer,
        @NotNull URI _schemaURL,
        @NotNull RunLink run,
        @NotNull JobLink job) {
      super(_producer, _schemaURL);
      this.run = run;
      this.job = job;
    }
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  public static class RunLink {

    @NotNull private String runId;
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  public static class JobLink {

    @NotNull private String namespace;
    @NotNull private String name;
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  public static class Job extends BaseJsonModel {

    @NotNull private String namespace;
    @NotNull private String name;
    @Valid private JobFacet facets;

    /**
     * Verifies if a job is a streaming job.
     *
     * @return
     */
    @JsonIgnore
    public boolean isStreamingJob() {
      return Optional.ofNullable(this.facets)
          .map(JobFacet::getJobType)
          .map(JobTypeJobFacet::getProcessingType)
          .filter(type -> type.equalsIgnoreCase("STREAMING"))
          .isPresent();
    }

    @JsonIgnore
    public JobType type() {
      return isStreamingJob() ? JobType.STREAM : JobType.BATCH;
    }
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Valid
  @ToString
  @JsonPropertyOrder({"documentation", "sourceCodeLocation", "sql", "description", "jobType"})
  public static class JobFacet {

    @Valid private DocumentationJobFacet documentation;
    @Valid private SourceCodeLocationJobFacet sourceCodeLocation;
    @Valid private SQLJobFacet sql;
    @Valid private JobTypeJobFacet jobType;
    @Builder.Default @JsonIgnore private Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    public void setFacet(String key, Object value) {
      additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFacets() {
      return additional;
    }

    public DocumentationJobFacet getDocumentation() {
      return documentation;
    }

    public SourceCodeLocationJobFacet getSourceCodeLocation() {
      return sourceCodeLocation;
    }

    public JobTypeJobFacet getJobType() {
      return jobType;
    }

    public SQLJobFacet getSql() {
      return sql;
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  public static class DocumentationJobFacet extends BaseFacet {

    @NotNull private String description;

    @Builder
    public DocumentationJobFacet(
        @NotNull URI _producer, @NotNull URI _schemaURL, @NotNull String description) {
      super(_producer, _schemaURL);
      this.description = description;
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  public static class SourceCodeLocationJobFacet extends BaseFacet {

    private String type;
    private String url;

    @Builder
    public SourceCodeLocationJobFacet(
        @NotNull URI _producer, @NotNull URI _schemaURL, String type, String url) {
      super(_producer, _schemaURL);
      this.type = type;
      this.url = url;
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  public static class SQLJobFacet extends BaseFacet {

    @NotNull private String query;

    @Builder
    public SQLJobFacet(@NotNull URI _producer, @NotNull URI _schemaURL, @NotNull String query) {
      super(_producer, _schemaURL);
      this.query = query;
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  public static class JobTypeJobFacet extends BaseFacet {

    @NotNull private String processingType;
    @NotNull private String integration;
    @NotNull private String jobType;

    @Builder
    public JobTypeJobFacet(
        @NotNull URI _producer,
        @NotNull URI _schemaURL,
        @NotNull String processingType,
        @NotNull String integration,
        @NotNull String jobType) {
      super(_producer, _schemaURL);
      this.processingType = processingType;
      this.integration = integration;
      this.jobType = jobType;
    }
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  @NotNull
  public static class Dataset extends BaseJsonModel {

    @NotNull private String namespace;
    @NotNull private String name;
    @Valid private DatasetFacets facets;
    @Valid private InputDatasetFacets inputFacets;
    @Valid private OutputDatasetFacets outputFacets;

    /**
     * Constructor with three args added manually to support dozens of existing usages created
     * before adding inputFacets and outputFacets, as Lombok does not provide SomeArgsConstructor.
     *
     * @param namespace
     * @param name
     * @param facets
     */
    public Dataset(String namespace, String name, DatasetFacets facets) {
      this.namespace = namespace;
      this.name = name;
      this.facets = facets;
    }
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Valid
  @ToString
  @JsonPropertyOrder({
    "documentation",
    "schema",
    "dataSource",
    "description",
    "lifecycleStateChange",
    "columnLineage",
    "symlinks"
  })
  public static class DatasetFacets {

    @Valid private DocumentationDatasetFacet documentation;
    @Valid private SchemaDatasetFacet schema;
    @Valid private LifecycleStateChangeFacet lifecycleStateChange;
    @Valid private DatasourceDatasetFacet dataSource;
    @Valid private LineageEvent.ColumnLineageDatasetFacet columnLineage;
    @Valid private DatasetSymlinkFacet symlinks;
    private String description;
    @Builder.Default @JsonIgnore private Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    public void setFacet(String key, Object value) {
      additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFacets() {
      return additional;
    }

    public DocumentationDatasetFacet getDocumentation() {
      return documentation;
    }

    public SchemaDatasetFacet getSchema() {
      return schema;
    }

    public DatasetSymlinkFacet getSymlinks() {
      return symlinks;
    }

    public LifecycleStateChangeFacet getLifecycleStateChange() {
      return lifecycleStateChange;
    }

    public DatasourceDatasetFacet getDataSource() {
      return dataSource;
    }

    public ColumnLineageDatasetFacet getColumnLineage() {
      return columnLineage;
    }

    public String getDescription() {
      return description;
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  public static class DocumentationDatasetFacet extends BaseFacet {

    @NotNull private String description;

    @Builder
    public DocumentationDatasetFacet(
        @NotNull URI _producer, @NotNull URI _schemaURL, @NotNull String description) {
      super(_producer, _schemaURL);
      this.description = description;
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  public static class SchemaDatasetFacet extends BaseFacet {

    @Valid private List<SchemaField> fields;

    @Builder
    public SchemaDatasetFacet(
        @NotNull URI _producer, @NotNull URI _schemaURL, List<SchemaField> fields) {
      super(_producer, _schemaURL);
      this.fields = fields;
    }
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  public static class SchemaField extends BaseJsonModel {

    @NotNull private String name;
    @Nullable private String type;
    private String description;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  public static class DatasetSymlinkFacet extends BaseFacet {

    @Valid private List<SymlinkIdentifier> identifiers;

    @Builder
    public DatasetSymlinkFacet(
        @NotNull URI _producer, @NotNull URI _schemaURL, List<SymlinkIdentifier> identifiers) {
      super(_producer, _schemaURL);
      this.identifiers = identifiers;
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  public static class SymlinkIdentifier extends BaseJsonModel {

    @NotNull private String namespace;
    @NotNull private String name;
    @Nullable private String type;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  public static class DatasourceDatasetFacet extends BaseFacet {

    private String name;
    private String uri;

    @Builder
    public DatasourceDatasetFacet(
        @NotNull URI _producer, @NotNull URI _schemaURL, String name, String uri) {
      super(_producer, _schemaURL);
      this.name = name;
      this.uri = uri;
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  public static class LifecycleStateChangeFacet extends BaseFacet {

    private String lifecycleStateChange;

    @Builder
    public LifecycleStateChangeFacet(
        @NotNull URI _producer, @NotNull URI _schemaURL, String lifecycleStateChange) {
      super(_producer, _schemaURL);
      this.lifecycleStateChange = lifecycleStateChange;
    }
  }

  @Getter
  @Setter
  @Valid
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ColumnLineageDatasetFacet extends BaseFacet {
    @Valid private ColumnLineageDatasetFacetFields fields;

    @Builder
    public ColumnLineageDatasetFacet(
        @NotNull URI _producer, @NotNull URI _schemaURL, ColumnLineageDatasetFacetFields fields) {
      super(_producer, _schemaURL);
      this.fields = fields;
    }
  }

  @Builder
  @Getter
  @Setter
  @Valid
  @ToString
  @NoArgsConstructor
  public static class ColumnLineageDatasetFacetFields {

    @Builder.Default @JsonIgnore
    private Map<String, ColumnLineageOutputColumn> additional = new LinkedHashMap<>();

    @JsonAnySetter
    public void setFacet(String key, ColumnLineageOutputColumn value) {
      additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, ColumnLineageOutputColumn> getAdditionalFacets() {
      return additional;
    }

    @Builder
    public ColumnLineageDatasetFacetFields(Map<String, ColumnLineageOutputColumn> additional) {
      this.additional = additional;
    }
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  public static class ColumnLineageOutputColumn extends BaseJsonModel {

    @NotNull private List<ColumnLineageInputField> inputFields;
    private String transformationDescription;
    private String transformationType;
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  public static class ColumnLineageInputField extends BaseJsonModel {

    @NotNull private String namespace;
    @NotNull private String name;
    @NotNull private String field;
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  public static class InputDatasetFacets {

    @Builder.Default @JsonIgnore private Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    public void setInputFacet(String key, Object value) {
      additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFacets() {
      return additional;
    }
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  public static class OutputDatasetFacets {

    @Builder.Default @JsonIgnore private Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    public void setOutputFacet(String key, Object value) {
      additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFacets() {
      return additional;
    }
  }
}
