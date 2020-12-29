package marquez.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.ToString;

/**
 * Requires jackson serialization features: mapper.registerModule(new JavaTimeModule());
 * mapper.setSerializationInclusion(Include.NON_NULL);
 * mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
 * mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
 */
@Valid
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineageEvent {

  public String eventType;
  @NotNull public ZonedDateTime eventTime;
  @NotNull public LineageRun run;
  @NotNull public LineageJob job;
  public List<LineageDataset> inputs;
  public List<LineageDataset> outputs;
  @NotNull public String producer;

  @Valid
  @ToString
  @NotNull
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class LineageRun {

    @NotNull public String runId;
    public RunFacet facets;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPropertyOrder({"nominalTime", "parent"})
  public static class RunFacet {

    public NominalTimeFacet nominalTime;
    public LineageRunParent parent;

    private Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    void setFacet(String key, Object value) {
      additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFacets() {
      return additional;
    }
  }

  public abstract static class BaseFacet {
    @NotNull public URI _producer;
    @NotNull public URI _schemaURL;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class NominalTimeFacet extends BaseFacet {

    @NotNull public ZonedDateTime nominalStartTime;
    public ZonedDateTime nominalEndTime;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class LineageRunParent extends BaseFacet {
    @NotNull public RunLink run;
    @NotNull public JobLink job;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class RunLink {
    @NotNull public String runId;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class JobLink {
    @NotNull public String namespace;
    @NotNull public String name;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class LineageJob {

    @NotNull public String namespace;
    @NotNull public String name;
    public JobFacet facets;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPropertyOrder({"documentation", "sourceCodeLocation", "sql", "description"})
  public static class JobFacet extends BaseFacet {

    public DocumentationFacet documentation;
    public SourceCodeLocationFacet sourceCodeLocation;
    public SqlFacet sql;
    private Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    void setFacet(String key, Object value) {
      additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFacets() {
      return additional;
    }
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DocumentationFacet extends BaseFacet {
    @NotNull public String description;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SourceCodeLocationFacet extends BaseFacet {

    public String type;
    public String url;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SqlFacet extends BaseFacet {
    @NotNull public String query;
  }

  @Valid
  @ToString
  @NotNull
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class LineageDataset {

    @NotNull public String namespace;
    @NotNull public String name;
    public DatasetFacet facets;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPropertyOrder({"documentation", "schema", "dataSource", "description"})
  public static class DatasetFacet {

    public DocumentationFacet documentation;
    public SchemaFacet schema;
    public DataSourceFacet dataSource;
    public String description;
    private Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    void setFacet(String key, Object value) {
      additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFacets() {
      return additional;
    }
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SchemaFacet extends BaseFacet {

    public List<SchemaField> fields;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SchemaField {

    @NotNull public String name;
    @NotNull public String type;
    public String description;
  }

  @Valid
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DataSourceFacet extends BaseFacet {

    public String name;
    public String uri;
  }
}
