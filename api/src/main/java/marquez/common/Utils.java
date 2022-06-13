/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.stream.Collectors.joining;
import static marquez.common.base.MorePreconditions.checkNotBlank;
import static marquez.common.models.DatasetType.DB_TABLE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import io.dropwizard.jackson.Jackson;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import marquez.common.models.DatasetId;
import marquez.common.models.Field;
import marquez.common.models.FlexibleDateTimeDeserializer;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.Version;
import marquez.service.models.DatasetMeta;
import marquez.service.models.DbTableMeta;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.ParentRunFacet;
import marquez.service.models.StreamMeta;
import org.apache.commons.lang3.tuple.Triple;

public final class Utils {
  private Utils() {}

  public static final String KV_DELIM = "#";
  public static final Joiner.MapJoiner KV_JOINER = Joiner.on(KV_DELIM).withKeyValueSeparator("=");
  public static final String VERSION_DELIM = ":";
  public static final Joiner VERSION_JOINER = Joiner.on(VERSION_DELIM).skipNulls();

  /**
   * pre-defined NAMESPACE_URL defined in RFC4122. This is the namespace used by the OpenLineage
   * Airflow integration for constructing some run IDs as UUIDs. We use the same namespace to
   * construct the same UUIDs when absolutely necessary (e.g., backfills, backward compatibility)
   *
   * @see "https://datatracker.ietf.org/doc/html/rfc4122#appendix-C"
   */
  public static final UUID NAMESPACE_URL_UUID =
      UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");

  private static final ObjectMapper MAPPER = newObjectMapper();

  private static final int UUID_LENGTH = 36;

  public static ObjectMapper newObjectMapper() {
    final ObjectMapper mapper = Jackson.newObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

    addZonedDateTimeMixin(mapper);
    return mapper;
  }

  /**
   * Add a mixin to the object mapper to support a {@link FlexibleDateTimeDeserializer}. This allows
   * us to support ISO timestamps that are missing timezones and defaults to the server timezone in
   * such cases.
   *
   * @param mapper
   */
  public static void addZonedDateTimeMixin(ObjectMapper mapper) {
    mapper.addMixIn(ZonedDateTime.class, ZonedDateTimeMixin.class);
  }

  @JsonDeserialize(using = FlexibleDateTimeDeserializer.class)
  static final class ZonedDateTimeMixin {}

  public static String toJson(@NonNull final Object value) {
    try {
      return MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static <T> T fromJson(@NonNull final String json, @NonNull final TypeReference<T> type) {
    try {
      return MAPPER.readValue(json, type);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static <T> T fromJson(
      @NonNull final InputStream json, @NonNull final TypeReference<T> type) {
    try {
      return MAPPER.readValue(json, type);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static ObjectMapper getMapper() {
    return MAPPER;
  }

  public static URL toUrl(@NonNull final String urlString) {
    checkNotBlank(urlString, "urlString must not be blank or empty");
    try {
      return new URL(urlString);
    } catch (MalformedURLException e) {
      final AssertionError error = new AssertionError("Malformed URL: " + urlString);
      error.initCause(e);
      throw error;
    }
  }

  public static String checksumFor(@NonNull final Map<String, String> kvMap) {
    return Hashing.sha256().hashString(KV_JOINER.join(kvMap), UTF_8).toString();
  }

  public static UUID toUuid(@NonNull final String uuidString) {
    checkNotBlank(uuidString, "uuidString must not be blank or empty");
    checkArgument(
        uuidString.length() == UUID_LENGTH,
        String.format("uuidString length must = %d", UUID_LENGTH));
    return UUID.fromString(uuidString);
  }

  /**
   * Construct a name-based {@link UUID} based on the {@link #NAMESPACE_URL_UUID} namespace. Name
   * parts are separated by a dot (.) character.
   *
   * @see "https://datatracker.ietf.org/doc/html/rfc4122#page-13"
   * @param nameParts
   * @return
   */
  public static UUID toNameBasedUuid(String... nameParts) {
    String constructedName = String.join(".", nameParts);

    final byte[] nameBytes = constructedName.getBytes(StandardCharsets.UTF_8);

    ByteBuffer buffer = ByteBuffer.allocate(nameBytes.length + 16);
    buffer.putLong(NAMESPACE_URL_UUID.getMostSignificantBits());
    buffer.putLong(NAMESPACE_URL_UUID.getLeastSignificantBits());
    buffer.put(nameBytes);

    return UUID.nameUUIDFromBytes(buffer.array());
  }

  /**
   * Construct a UUID from a {@link ParentRunFacet} - if the {@link
   * marquez.service.models.LineageEvent.RunLink#runId} field is a valid {@link UUID}, use it.
   * Otherwise, compute a {@link UUID} from the job name and the reported runId. If the job name
   * contains a dot (.), only return the portion up to the last dot in the name (this attempts to
   * address airflow tasks, which always report the job name as &lt;dag_name&gt;.&lt;task_name&lt;
   *
   * @param parent
   * @return
   */
  public static UUID findParentRunUuid(ParentRunFacet parent) {
    String jobName = parent.getJob().getName();
    String parentRunId = parent.getRun().getRunId();
    return findParentRunUuid(jobName, parentRunId);
  }

  public static UUID findParentRunUuid(String parentJobName, String parentRunId) {
    String dagName = parseParentJobName(parentJobName);
    return toUuid(parentRunId, dagName);
  }

  public static String parseParentJobName(String parentJobName) {
    return parentJobName.contains(".")
        ? parentJobName.substring(0, parentJobName.lastIndexOf('.'))
        : parentJobName;
  }

  /**
   * Compute a UUID from a RunId and a jobName
   *
   * @see Utils#toNameBasedUuid(String...) for details on the UUID construction.
   * @param runId
   * @param jobName
   * @return
   */
  public static UUID toUuid(@NotNull String runId, String jobName) {
    try {
      return UUID.fromString(runId);
    } catch (IllegalArgumentException e) {
      return Utils.toNameBasedUuid(jobName, runId);
    }
  }

  public static Instant toInstant(@Nullable final String asIso) {
    return (asIso == null) ? null : Instant.from(ISO_INSTANT.parse(asIso));
  }

  /**
   * Returns a new {@link Version} object based on the job's namespace, name, inputs and inputs,
   * source code location, and context. A {@link Version} is generated by concatenating the provided
   * job meta together (delimited by a comma). The resulting string is then converted to a {@code
   * byte} array and passed to {@link UUID#nameUUIDFromBytes(byte[])}.
   *
   * @param namespaceName The namespace of the job.
   * @param jobName The name of the job.
   * @param jobInputIds The input dataset IDs for the job.
   * @param jobOutputIds The output dataset IDs for the job.
   * @param jobContext The context of the job.
   * @param jobLocation The source code location for the job.
   * @return A {@link Version} object based on the specified job meta.
   */
  public static Version newJobVersionFor(
      @NonNull final NamespaceName namespaceName,
      @NonNull final JobName jobName,
      @NonNull final ImmutableSet<DatasetId> jobInputIds,
      @NonNull final ImmutableSet<DatasetId> jobOutputIds,
      @NonNull final ImmutableMap<String, String> jobContext,
      @Nullable final String jobLocation) {
    final byte[] bytes =
        VERSION_JOINER
            .join(
                namespaceName.getValue(),
                jobName.getValue(),
                jobInputIds.stream()
                    .sorted()
                    .flatMap(
                        jobInputId ->
                            Stream.of(
                                jobInputId.getNamespace().getValue(),
                                jobInputId.getName().getValue()))
                    .collect(joining(VERSION_DELIM)),
                jobOutputIds.stream()
                    .sorted()
                    .flatMap(
                        jobOutputId ->
                            Stream.of(
                                jobOutputId.getNamespace().getValue(),
                                jobOutputId.getName().getValue()))
                    .collect(joining(VERSION_DELIM)),
                jobLocation,
                KV_JOINER.join(jobContext))
            .getBytes(UTF_8);
    return Version.of(UUID.nameUUIDFromBytes(bytes));
  }

  /**
   * Returns a new {@link Version} object based on the dataset namespace, source name, physical
   * name, dataset name dataset fields amd run Id. Used for Openlineage events. A {@link Version} is
   * generated by concatenating the provided job meta together (delimited by a colon). The resulting
   * string is then converted to a {@code byte} array and passed to {@link
   * UUID#nameUUIDFromBytes(byte[])}.
   *
   * @param namespace The namespace of the dataset.
   * @param sourceName The source name of the dataset.
   * @param physicalName The physical name of the dataset.
   * @param datasetName The dataset name.
   * @param lifecycleState The dataset change like CREATE, DROP, TRUNCATE.
   * @param fields The fields of the dataset.
   * @param runId The UUID of the run linked to the dataset.
   * @return A {@link Version} object based on the specified job meta.
   */
  public static Version newDatasetVersionFor(
      String namespace,
      String sourceName,
      String physicalName,
      String datasetName,
      String lifecycleState,
      List<LineageEvent.SchemaField> fields,
      UUID runId) {
    DatasetVersionData data =
        DatasetVersionData.builder()
            .namespace(namespace)
            .sourceName(sourceName)
            .physicalName(physicalName)
            .datasetName(datasetName)
            .lifecycleState(lifecycleState)
            .schemaFields(fields)
            .runId(runId)
            .build();
    return newDatasetVersionFor(data);
  }

  /**
   * Returns a new {@link Version} object based on the dataset namespace, dataset name dataset meta
   * information. A {@link Version} is generated by concatenating the provided job meta together
   * (delimited by a colon). The resulting string is then converted to a {@code byte} array and
   * passed to {@link UUID#nameUUIDFromBytes(byte[])}.
   *
   * @param namespaceName The namespace of the dataset.
   * @param datasetName The dataset name.
   * @param datasetMeta The meta information of the dataset. Like fields, tags, physical name.
   * @return A {@link Version} object based on the specified job meta.
   */
  public static Version newDatasetVersionFor(
      String namespaceName, String datasetName, DatasetMeta datasetMeta) {
    DatasetVersionData datasetVersionData =
        DatasetVersionData.builder()
            .datasetName(datasetName)
            .namespace(namespaceName)
            .datasetMeta(datasetMeta)
            .build();
    return newDatasetVersionFor(datasetVersionData);
  }

  private static Version newDatasetVersionFor(DatasetVersionData data) {
    final byte[] bytes =
        VERSION_JOINER
            .join(
                data.getNamespace(),
                data.getSourceName(),
                data.getDatasetName(),
                data.getPhysicalName(),
                data.getSchemaLocation(),
                data.getFields().stream().map(Utils::joinField).collect(joining(VERSION_DELIM)),
                data.getLifecycleState(),
                data.getRunId())
            .getBytes(UTF_8);
    return Version.of(UUID.nameUUIDFromBytes(bytes));
  }

  private static String joinField(Triple<String, String, String> field) {
    return VERSION_JOINER.join(field.getLeft(), field.getMiddle(), field.getRight());
  }

  @Getter
  @Builder
  private static class DatasetVersionData {
    private String namespace;
    private String sourceName;
    private String physicalName;
    private String datasetName;
    private String lifecycleState;
    private String schemaLocation;
    private Set<Triple<String, String, String>> fields;
    private UUID runId;

    public static class DatasetVersionDataBuilder {
      private static final Function<LineageEvent.SchemaField, Triple<String, String, String>>
          schemaFieldToTripleFunction =
              f ->
                  Triple.of(
                      f.getName(),
                      f.getType() == null ? null : f.getType().toUpperCase(),
                      f.getDescription());
      private static final Function<Field, Triple<String, String, String>> fieldToTripleFunction =
          f ->
              Triple.of(
                  f.getName().getValue(),
                  f.getType() == null ? null : f.getType().toUpperCase(),
                  f.getDescription().orElse(null));

      private String sourceName;
      private String physicalName;
      private String schemaLocation;
      private Set<Triple<String, String, String>> fields = ImmutableSet.of();
      private UUID runId;

      DatasetVersionData.DatasetVersionDataBuilder schemaFields(
          List<LineageEvent.SchemaField> schemaFields) {
        if (schemaFields == null) return this;
        setFields(schemaFields, schemaFieldToTripleFunction);
        return this;
      }

      DatasetVersionData.DatasetVersionDataBuilder streamMeta(StreamMeta streamMeta) {
        this.sourceName = streamMeta.getSourceName().getValue();
        this.physicalName = streamMeta.getPhysicalName().getValue();
        this.schemaLocation = streamMeta.getSchemaLocation().toString();
        fields(streamMeta.getFields());
        return this;
      }

      DatasetVersionData.DatasetVersionDataBuilder datasetMeta(DatasetMeta datasetMeta) {
        if (datasetMeta == null) return this;
        return datasetMeta.getType().equals(DB_TABLE)
            ? dbTableMeta((DbTableMeta) datasetMeta)
            : streamMeta((StreamMeta) datasetMeta);
      }

      DatasetVersionData.DatasetVersionDataBuilder dbTableMeta(DbTableMeta tableMeta) {
        this.sourceName = tableMeta.getSourceName().getValue();
        this.physicalName = tableMeta.getPhysicalName().getValue();
        fields(tableMeta.getFields());
        this.runId = tableMeta.getRunId().map(RunId::getValue).orElse(null);
        return this;
      }

      DatasetVersionData.DatasetVersionDataBuilder fields(List<Field> fields) {
        if (fields == null) return this;
        setFields(fields, fieldToTripleFunction);
        return this;
      }

      private <T> void setFields(
          List<T> fields, Function<T, Triple<String, String, String>> mapper) {
        if (!this.fields.isEmpty()) {
          throw new IllegalStateException(
              "'fields' and 'schemaFields' methods are mutually exclusive");
        }
        this.fields = fields.stream().map(mapper).collect(Collectors.toCollection(TreeSet::new));
      }
    }
  }
}
