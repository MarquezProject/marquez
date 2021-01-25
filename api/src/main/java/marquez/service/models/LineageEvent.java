package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineageEvent {

  private String eventType;
  @NotNull private ZonedDateTime eventTime;
  @NotNull private LineageEvent.Run run;
  @NotNull private LineageEvent.Job job;
  private List<Dataset> inputs;
  private List<Dataset> outputs;
  @NotNull private String producer;

  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Getter
  @Valid
  @ToString
  @NotNull
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Run {

    @NotNull private String runId;
    private RunFacet facets;
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPropertyOrder({"nominalTime", "parent"})
  public static class RunFacet {

    private NominalTimeRunFacet nominalTime;
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
  @JsonIgnoreProperties(ignoreUnknown = true)
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
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ParentRunFacet extends BaseFacet {

    @NotNull private RunLink run;
    @NotNull private JobLink job;

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
  @JsonIgnoreProperties(ignoreUnknown = true)
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
  @JsonIgnoreProperties(ignoreUnknown = true)
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
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Job {

    @NotNull private String namespace;
    @NotNull private String name;
    private JobFacet facets;
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPropertyOrder({"documentation", "sourceCodeLocation", "sql", "description"})
  public static class JobFacet {

    private DocumentationJobFacet documentation;
    private SourceCodeLocationJobFacet sourceCodeLocation;
    private SQLJobFacet sql;
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

    public SQLJobFacet getSql() {
      return sql;
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
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
  @JsonIgnoreProperties(ignoreUnknown = true)
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
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SQLJobFacet extends BaseFacet {

    @NotNull private String query;

    @Builder
    public SQLJobFacet(@NotNull URI _producer, @NotNull URI _schemaURL, @NotNull String query) {
      super(_producer, _schemaURL);
      this.query = query;
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
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Dataset {

    @NotNull private String namespace;
    @NotNull private String name;
    private DatasetFacet facets;
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPropertyOrder({"documentation", "schema", "dataSource", "description"})
  public static class DatasetFacet {

    private DocumentationDatasetFacet documentation;
    private SchemaDatasetFacet schema;
    private DatasourceDatasetFacet dataSource;
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

    public DatasourceDatasetFacet getDataSource() {
      return dataSource;
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
  @JsonIgnoreProperties(ignoreUnknown = true)
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
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SchemaDatasetFacet extends BaseFacet {

    private List<SchemaField> fields;

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
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SchemaField {

    @NotNull private String name;
    @NotNull private String type;
    private String description;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
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
}
