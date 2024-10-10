/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.models;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.Utils.toJson;
import static marquez.common.models.DatasetType.DB_TABLE;

import com.google.common.collect.ImmutableSet;
import io.openlineage.server.OpenLineage;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.api.exceptions.FacetNotValid;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.common.models.SourceName;

/**
 * Utility class for {@link OpenLineage.RunEvent}, {@link OpenLineage.JobEvent}, and {@link
 * OpenLineage.DatasetEvent}.
 */
public final class Metadata {
  private Metadata() {}

  /** Represents the parent run associated with {@link Metadata.Run}. */
  @Builder
  @ToString
  public static final class ParentRun {
    @Getter private final RunId id; // Unique parent run ID.
    @Getter private final ParentRun.Job job;

    /** Represents the parent job (if present) associated with {@link Metadata.Run}. */
    @Builder
    @ToString
    public static final class Job {
      @Getter private final JobId id;
      @Getter private final JobName name;
      @Getter private final NamespaceName namespace;

      static Job of(@NonNull final JobId id) {
        return Job.builder().id(id).name(id.getName()).namespace(id.getNamespace()).build();
      }
    }
  }

  /** Wrapper around a {@link OpenLineage.RunEvent}. */
  @Builder
  @ToString
  public static final class Run {
    @Nullable private final ParentRun parent;
    @Getter private final RunId id;
    @Getter private final RunState state;
    @Getter private final Instant transitionedOn;
    @Nullable private final Instant startedAt;
    @Nullable private final Instant endedAt;
    @Nullable private final Instant nominalStartTime;
    @Nullable private final Instant nominalEndTime;
    @Nullable private final String externalId;
    @Getter private final Job job;
    @Nullable private final IO io;
    @Getter private final String rawMeta;
    @Getter private final URI producer;

    public static Run forEvent(@NonNull final OpenLineage.RunEvent event) {
      final OpenLineage.Run run = event.getRun();
      final RunId runId = RunId.of(run.getRunId());
      final RunState runState = RunState.forType(event.getEventType());
      final Instant runTransitionedOnAsUtc = toUtc(event.getEventTime());
      final RunBuilder runBuilder =
          Run.builder()
              .parent(Facets.parentRunFor(run).orElse(null))
              .id(runId)
              .state(runState)
              .transitionedOn(runTransitionedOnAsUtc)
              .nominalStartTime(Facets.nominalStartTimeFor(run).orElse(null))
              .nominalEndTime(Facets.nominalEndTimeFor(run).orElse(null))
              .rawMeta(toJson(event))
              .producer(event.getProducer());

      if (runState.isStarting()) {
        runBuilder.startedAt(runTransitionedOnAsUtc);
      } else if (runState.isDone()) {
        runBuilder.endedAt(runTransitionedOnAsUtc);
      }

      final Job job =
          Job.newInstanceWith(run, event.getJob(), event.getInputs(), event.getOutputs());

      runBuilder.job(job);
      runBuilder.io(job.getIo().orElse(null));
      return runBuilder.build();
    }

    public Optional<ParentRun> getParent() {
      return Optional.ofNullable(parent);
    }

    public Optional<Instant> getNominalStartTime() {
      return Optional.ofNullable(nominalStartTime);
    }

    public Optional<Instant> getNominalEndTime() {
      return Optional.ofNullable(nominalEndTime);
    }

    public Optional<Instant> getStartedAt() {
      return Optional.ofNullable(startedAt);
    }

    public Optional<Instant> getEndedAt() {
      return Optional.ofNullable(endedAt);
    }

    public Optional<String> getExternalId() {
      return Optional.ofNullable(externalId);
    }

    public Optional<IO> getIo() {
      return Optional.ofNullable(io);
    }

    static Instant toUtc(@NonNull final ZonedDateTime transitionedOn) {
      return transitionedOn.withZoneSameInstant(ZoneOffset.UTC).toInstant();
    }
  }

  @Builder
  @ToString
  public static final class Job {
    @Getter private final JobId id;
    @Getter private final JobType type;
    @Getter private final JobName name;
    @Getter private final NamespaceName namespace;
    @Getter private final JobVersionId versionId;
    @Nullable private final String description;
    @Nullable private final SourceCode sourceCode;
    @Nullable private final SourceCodeLocation sourceCodeLocation;
    @Nullable private final IO io;

    public static Job newInstanceWith(
        @NonNull final OpenLineage.Run run,
        @NonNull final OpenLineage.Job job,
        @NonNull final List<OpenLineage.InputDataset> inputs,
        @NonNull final List<OpenLineage.OutputDataset> outputs) {
      return newJobWith(job, IO.newInstanceWith(run, inputs, outputs));
    }

    public static Job forEvent(@NonNull final OpenLineage.JobEvent event) {
      return newJobWith(event.getJob(), IO.newInstanceWith(event.getInputs(), event.getOutputs()));
    }

    static Job newJobWith(@NonNull final OpenLineage.Job job, @NonNull Metadata.IO io) {
      final JobId jobId = JobId.of(NamespaceName.of(job.getNamespace()), JobName.of(job.getName()));
      final Job.JobBuilder jobBuilder =
          Job.builder()
              .id(jobId)
              .type(JobType.BATCH)
              .name(jobId.getName())
              .namespace(jobId.getNamespace())
              .io(io);

      Facets.descriptionFor(job).ifPresent(jobBuilder::description);
      Facets.sourceCodeFor(job).ifPresent(jobBuilder::sourceCode);

      final Optional<SourceCodeLocation> jobSourceCodeLocation = Facets.sourceCodeLocationFor(job);
      jobSourceCodeLocation.ifPresent(jobBuilder::sourceCodeLocation);

      final JobVersionId jobVersionId =
          VersionId.forJob(jobId, jobSourceCodeLocation.orElse(null), io);

      return jobBuilder.versionId(jobVersionId).build();
    }

    public Optional<String> getDescription() {
      return Optional.ofNullable(description);
    }

    public Optional<SourceCode> getSourceCode() {
      return Optional.ofNullable(sourceCode);
    }

    public Optional<SourceCodeLocation> getSourceCodeLocation() {
      return Optional.ofNullable(sourceCodeLocation);
    }

    public Optional<IO> getIo() {
      return Optional.ofNullable(io);
    }

    @Builder
    @ToString
    @EqualsAndHashCode
    public static final class SourceCode {
      @Getter private final String language;
      @Getter private final String sourceCode;
    }

    @Builder
    @ToString
    @EqualsAndHashCode
    public static final class SourceCodeLocation {
      @Getter private final String type;
      @Getter private final URL url;
      @Nullable private final URL repoUrl;
      @Nullable private final String path;
      @Nullable private final String version;
      @Nullable private final String tag;
      @Nullable private final String branch;

      public Optional<URL> getRepoUrl() {
        return Optional.ofNullable(repoUrl);
      }

      public Optional<String> getPath() {
        return Optional.ofNullable(path);
      }

      public Optional<String> getVersion() {
        return Optional.ofNullable(version);
      }

      public Optional<String> getTag() {
        return Optional.ofNullable(tag);
      }

      public Optional<String> getBranch() {
        return Optional.ofNullable(branch);
      }
    }
  }

  @Builder
  @ToString
  @EqualsAndHashCode
  public static final class Dataset {
    @Getter private final DatasetId id;
    @Getter private final DatasetType type;
    @Getter private final DatasetName name;
    @Getter private final NamespaceName namespace;
    @Getter private final DatasetVersionId versionId;
    @Nullable private final Schema schema;
    @Getter private final Source source;

    public static Dataset forEvent(@NonNull final OpenLineage.DatasetEvent event) {
      return newInstanceWith(null, event.getDataset());
    }

    static Dataset newInstanceFor(@NonNull final OpenLineage.Dataset dataset) {
      return newInstanceWith(null, dataset);
    }

    static Dataset newInstanceWith(
        @Nullable final OpenLineage.Run run, @NonNull final OpenLineage.Dataset dataset) {
      final DatasetId datasetId =
          new DatasetId(
              NamespaceName.of(dataset.getNamespace()), DatasetName.of(dataset.getName()));
      final Dataset.DatasetBuilder datasetBuilder =
          Dataset.builder()
              .id(datasetId)
              .type(DB_TABLE)
              .name(datasetId.getName())
              .namespace(datasetId.getNamespace());

      final Dataset.Source source = Facets.sourceFor(dataset);
      final Optional<Dataset.Schema> datasetSchema = Facets.schemaFor(dataset);
      final DatasetVersionId datasetVersionId =
          VersionId.forDataset(datasetId, datasetSchema.orElse(null), source);

      return datasetBuilder.source(source).versionId(datasetVersionId).build();
    }

    @Builder
    @ToString
    public static class Schema {
      @Getter @NonNull private final ImmutableSet<Schema.Field> fields;

      @Builder
      @ToString
      @EqualsAndHashCode
      public static class Field {
        @Getter private final String name;
        @Getter private final String type;
        @Nullable private final String description;

        public Optional<String> getDescription() {
          return Optional.ofNullable(description);
        }
      }
    }

    @Builder
    @ToString
    public static class Source {
      @Getter private final SourceName name;
      @Getter private final URI connectionUrl;
    }

    public Optional<Dataset.Schema> getSchema() {
      return Optional.ofNullable(schema);
    }
  }

  @ToString
  public static final class IO {
    @Getter ImmutableSet<Dataset> inputs;
    @Getter ImmutableSet<Dataset> outputs;

    IO(@NonNull final ImmutableSet<Dataset> inputs, @NonNull final ImmutableSet<Dataset> outputs) {
      this.inputs = inputs;
      this.outputs = outputs;
    }

    public static IO newInstanceWith(
        @NonNull final List<OpenLineage.InputDataset> inputs,
        @NonNull final List<OpenLineage.OutputDataset> outputs) {
      return newInstanceWith(null, inputs, outputs);
    }

    public static IO newInstanceWith(
        @Nullable final OpenLineage.Run run,
        @NonNull final List<OpenLineage.InputDataset> inputs,
        @NonNull final List<OpenLineage.OutputDataset> outputs) {
      final ImmutableSet.Builder<Dataset> inputsBuilder = ImmutableSet.builder();
      for (final OpenLineage.InputDataset input : inputs) {
        inputsBuilder.add(Dataset.newInstanceFor(input));
      }
      final ImmutableSet.Builder<Dataset> outputsBuilder = ImmutableSet.builder();
      for (final OpenLineage.OutputDataset output : outputs) {
        outputsBuilder.add(Dataset.newInstanceWith(run, output));
      }
      return new IO(inputsBuilder.build(), outputsBuilder.build());
    }
  }

  static class Facets {
    static final class Job {
      static final String SOURCE_CODE = "sourceCode";
      static final String SOURCE_CODE_LANGUAGE = "language";
      static final String SOURCE_CODE_LOCATION = "sourceCodeLocation";
      static final String SOURCE_CODE_TYPE = "type";
      static final String SOURCE_CODE_URL = "url";
      static final String SOURCE_CODE_REPO_URL = "repoUrl";
      static final String SOURCE_CODE_PATH = "path";
      static final String SOURCE_CODE_VERSION = "version";
      static final String SOURCE_CODE_TAG = "tag";
      static final String SOURCE_CODE_BRANCH = "branch";
      static final String DOCUMENTATION = "documentation";
      static final String DESCRIPTION = "description";
    }

    static final class Run {
      static final String PARENT = "parent";
      static final String PARENT_RUN = "run";
      static final String PARENT_RUN_ID = "runId";
      static final String PARENT_JOB = "job";
      static final String PARENT_JOB_NAME = "name";
      static final String PARENT_JOB_NAMESPACE = "namespace";
      static final String NOMINAL_TIME = "nominalTime";
      static final String NOMINAL_START_TIME = "nominalStartTime";
      static final String NOMINAL_END_TIME = "nominalEndTime";
    }

    static final class Dataset {
      static final String SOURCE = "dataSource";
      static final String SOURCE_NAME = "name";
      static final String SOURCE_CONNECTION_URI = "uri";
      static final String SCHEMA = "schema";
      static final String SCHEMA_FIELDS = "fields";
      static final String FIELD_TYPE = "type";
      static final String FIELD_NAME = "name";
      static final String FIELD_DESCRIPTION = "description";
    }

    Facets() {}

    /**
     * Returns {@link Metadata.ParentRun} (if present) for the given {@link OpenLineage.Run} object.
     *
     * @see <a href="https://openlineage.io/spec/facets/1-0-1/ParentRunFacet.json">ParentRunFacet/a>
     */
    @SuppressWarnings("unchecked") // Safely cast
    static Optional<Metadata.ParentRun> parentRunFor(@NonNull final OpenLineage.Run run) {
      return Optional.ofNullable(run.getFacets())
          .map(OpenLineage.RunFacets::getAdditionalProperties)
          .map(runFacets -> runFacets.get(Facets.Run.PARENT))
          .map(OpenLineage.RunFacet::getAdditionalProperties)
          .flatMap(
              parentRunFacet -> {
                // Extract runID for parent run (required)
                final RunId parentRunId =
                    Optional.ofNullable(parentRunFacet.get(Facets.Run.PARENT_RUN))
                        .map(
                            parentRun ->
                                ((Map<String, Object>) parentRun).get(Facets.Run.PARENT_RUN_ID))
                        .map(String::valueOf)
                        .map(RunId::new)
                        .orElseThrow(() -> new FacetNotValid.MissingRunIdForParent(run.getRunId()));
                // Extract job for parent (required).
                final ParentRun.Job parentJob =
                    Optional.ofNullable(parentRunFacet.get(Facets.Run.PARENT_JOB))
                        .map(
                            parentJobForRun -> {
                              // Extract parent job namespace (required).
                              final String parentJobNamespace =
                                  Optional.ofNullable(
                                          ((Map<String, Object>) parentJobForRun)
                                              .get(Facets.Run.PARENT_JOB_NAMESPACE))
                                      .map(String::valueOf)
                                      .orElseThrow(
                                          () ->
                                              new FacetNotValid.MissingNamespaceForParentJob(
                                                  run.getRunId()));
                              // Extract parent job name (required).
                              final String parentJobName =
                                  Optional.ofNullable(
                                          ((Map<String, Object>) parentJobForRun)
                                              .get(Facets.Run.PARENT_JOB_NAME))
                                      .map(String::valueOf)
                                      .orElseThrow(
                                          () ->
                                              new FacetNotValid.MissingNameForParentJob(
                                                  run.getRunId()));
                              // ParentRun.Job instance with namespace and name of parent job
                              // (required).
                              return Metadata.ParentRun.Job.of(
                                  JobId.of(parentJobNamespace, parentJobName));
                            })
                        .orElseThrow(() -> new FacetNotValid.MissingJobForParent(run.getRunId()));
                // Metadata.ParentRun instance (if present) with unique run ID and job of parent run
                // (required).
                return Optional.of(
                    Metadata.ParentRun.builder().id(parentRunId).job(parentJob).build());
              });
    }

    /**
     * Returns nominal {@code startTime} (if present) for the given {@link OpenLineage.Run} object.
     *
     * @see <a
     *     href="https://openlineage.io/spec/facets/1-0-1/NominalTimeRunFacet.json">NominalTimeRunFacet/a>
     */
    static Optional<Instant> nominalStartTimeFor(@NonNull final OpenLineage.Run run) {
      return Optional.ofNullable(run.getFacets())
          .map(OpenLineage.RunFacets::getAdditionalProperties)
          .map(facets -> facets.get(Facets.Run.NOMINAL_TIME))
          .map(OpenLineage.RunFacet::getAdditionalProperties)
          .map(
              nominalTimeFacet -> {
                // Extract nominal start time for run (required).
                final String nominalStartTime =
                    Optional.ofNullable(nominalTimeFacet.get(Facets.Run.NOMINAL_START_TIME))
                        .map(String::valueOf)
                        .orElseThrow(
                            () -> new FacetNotValid.MissingNominalStartTimeForRun(run.getRunId()));
                return ZonedDateTime.parse(nominalStartTime)
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toInstant();
              });
    }

    /**
     * Returns nominal {@code endTime} (if present) for the given {@link OpenLineage.Run} object.
     *
     * @see <a href="
     *     https://openlineage.io/spec/facets/1-0-1/NominalTimeRunFacet.json">NominalTimeRunFacet/a>
     */
    static Optional<Instant> nominalEndTimeFor(@NonNull final OpenLineage.Run run) {
      return Optional.ofNullable(run.getFacets())
          .map(OpenLineage.RunFacets::getAdditionalProperties)
          .map(runFacets -> runFacets.get(Facets.Run.NOMINAL_TIME))
          .map(OpenLineage.RunFacet::getAdditionalProperties)
          .map(nominalTimeFacet -> nominalTimeFacet.get(Facets.Run.NOMINAL_END_TIME))
          .map(
              nominalEndTime ->
                  ZonedDateTime.parse(
                          (String) nominalEndTime) // Extracted nominal end time for run (optional).
                      .withZoneSameInstant(ZoneOffset.UTC)
                      .toInstant());
    }

    /**
     * Returns job {@code source code} (if present) for the given {@link OpenLineage.Job} object.
     *
     * @see <a href="
     *     https://openlineage.io/spec/facets/1-0-1/SourceCodeJobFacet.json">SourceCodeJobFacet/a>
     */
    static Optional<Metadata.Job.SourceCode> sourceCodeFor(@NonNull final OpenLineage.Job job) {
      return Optional.ofNullable(job.getFacets())
          .map(OpenLineage.JobFacets::getAdditionalProperties)
          .map(jobFacets -> jobFacets.get(Facets.Job.SOURCE_CODE))
          .map(OpenLineage.JobFacet::getAdditionalProperties)
          .flatMap(
              sourceCodeFacet -> {
                // Extract source code language for job (required).
                final String sourceCodeLanguage =
                    Optional.ofNullable(sourceCodeFacet.get(Facets.Job.SOURCE_CODE_LANGUAGE))
                        .map(String::valueOf)
                        .orElseThrow(
                            () -> new FacetNotValid.MissingSourceCodeLanguageForJob(job.getName()));
                // Extract source code for job (required).
                final String sourceCode =
                    Optional.ofNullable(sourceCodeFacet.get(Facets.Job.SOURCE_CODE))
                        .map(String::valueOf)
                        .orElseThrow(
                            () -> new FacetNotValid.MissingSourceCodeForJob(job.getName()));
                // Metadata.Job.SourceCode instance (if present) with source code language and
                // source code for job (required).
                return Optional.of(
                    Metadata.Job.SourceCode.builder()
                        .language(sourceCodeLanguage)
                        .sourceCode(sourceCode)
                        .build());
              });
    }

    /**
     * Returns {@code Metadata.Job.SourceCodeLocation} (if present) for the given {@link
     * OpenLineage.Job} object.
     *
     * @see <a
     *     href="https://openlineage.io/spec/facets/1-0-1/SourceCodeLocationJobFacet.json">SourceCodeLocationJobFacet/a>
     */
    static Optional<Metadata.Job.SourceCodeLocation> sourceCodeLocationFor(
        @NonNull final OpenLineage.Job job) {
      return Optional.ofNullable(job.getFacets())
          .map(OpenLineage.JobFacets::getAdditionalProperties)
          .map(jobFacets -> jobFacets.get(Facets.Job.SOURCE_CODE_LOCATION))
          .map(OpenLineage.JobFacet::getAdditionalProperties)
          .flatMap(
              sourceCodeLocationFacet -> {
                // Extract source code type for job location (required).
                final String sourceCodeType =
                    Optional.ofNullable(sourceCodeLocationFacet.get(Facets.Job.SOURCE_CODE_TYPE))
                        .map(String::valueOf)
                        .orElseThrow(
                            () -> new FacetNotValid.MissingSourceCodeTypeForJob(job.getName()));

                // Extract source code URL for job (required).
                final URL sourceCodeUrl =
                    Optional.ofNullable(sourceCodeLocationFacet.get(Facets.Job.SOURCE_CODE_URL))
                        .map(String::valueOf)
                        .map(Utils::toUrl)
                        .orElseThrow(
                            () -> new FacetNotValid.MissingSourceCodeUrlForJob(job.getName()));

                // Extract source code repo URL for job (optional).
                final Optional<URL> sourceCodeRepoUrl =
                    Optional.ofNullable(
                            sourceCodeLocationFacet.get(Facets.Job.SOURCE_CODE_REPO_URL))
                        .map(String::valueOf)
                        .map(Utils::toUrl);

                // Extract source code path in repo for job (optional).
                final Optional<String> sourceCodePath =
                    Optional.ofNullable(sourceCodeLocationFacet.get(Facets.Job.SOURCE_CODE_PATH))
                        .map(String::valueOf);

                // Extract source code version for job (optional).
                final Optional<String> sourceCodeVersion =
                    Optional.ofNullable(sourceCodeLocationFacet.get(Facets.Job.SOURCE_CODE_VERSION))
                        .map(String::valueOf);

                // Extract source code tag for job (optional).
                final Optional<String> sourceCodeTag =
                    Optional.ofNullable(sourceCodeLocationFacet.get(Facets.Job.SOURCE_CODE_TAG))
                        .map(String::valueOf);

                // Extract source code branch for job (optional).
                final Optional<String> sourceCodeBranch =
                    Optional.ofNullable(sourceCodeLocationFacet.get(Facets.Job.SOURCE_CODE_BRANCH))
                        .map(String::valueOf);

                // Metadata.Job.SourceCodeLocation instance (if present) with source code type and
                // URL for job (required).
                final Metadata.Job.SourceCodeLocation.SourceCodeLocationBuilder
                    sourceCodeLocationBuilder =
                        Metadata.Job.SourceCodeLocation.builder()
                            .type(sourceCodeType)
                            .url(sourceCodeUrl);

                // (optional)
                sourceCodeRepoUrl.ifPresent(sourceCodeLocationBuilder::repoUrl);
                sourceCodePath.ifPresent(sourceCodeLocationBuilder::path);
                sourceCodeVersion.ifPresent(sourceCodeLocationBuilder::version);
                sourceCodeTag.ifPresent(sourceCodeLocationBuilder::tag);
                sourceCodeBranch.ifPresent(sourceCodeLocationBuilder::branch);

                return Optional.of(sourceCodeLocationBuilder.build());
              });
    }

    /**
     * Returns {@code documentation} (if present) for the given {@link OpenLineage.Job} object.
     *
     * @see <a href="
     *     https://openlineage.io/spec/facets/1-0-1/DocumentationJobFacet.json">DocumentationJobFacet/a>
     */
    static Optional<String> descriptionFor(@NonNull final OpenLineage.Job job) {
      return Optional.ofNullable(job.getFacets())
          .map(OpenLineage.JobFacets::getAdditionalProperties)
          .map(jobFacets -> jobFacets.get(Facets.Job.DOCUMENTATION))
          .map(OpenLineage.JobFacet::getAdditionalProperties)
          .map(
              docFacet -> {
                // Extracted description for job (optional)
                return Optional.ofNullable(docFacet.get(Facets.Job.DESCRIPTION))
                    .map(String::valueOf)
                    .orElseThrow(() -> new FacetNotValid.MissingDescriptionForJob(job.getName()));
              });
    }

    /**
     * Returns {@link Metadata.Dataset.Schema} (if present) for the given {@link
     * OpenLineage.Dataset} object.
     *
     * @see <a
     *     href="https://openlineage.io/spec/facets/1-1-1/SchemaDatasetFacet.json">SchemaDatasetFacet/a>
     */
    @SuppressWarnings("unchecked")
    static Optional<Metadata.Dataset.Schema> schemaFor(@NonNull final OpenLineage.Dataset dataset) {
      return Optional.ofNullable(dataset.getFacets())
          .map(OpenLineage.DatasetFacets::getAdditionalProperties)
          .map(datasetFacets -> datasetFacets.get(Facets.Dataset.SCHEMA))
          .map(OpenLineage.DatasetFacet::getAdditionalProperties)
          .map(datasetSchemaFacet -> datasetSchemaFacet.get(Facets.Dataset.SCHEMA_FIELDS))
          .map(
              datasetSchema ->
                  ((List<Map<?, ?>>) datasetSchema)
                      .stream()
                          .map(
                              datasetField -> {
                                // Extract name for dataset field (required).
                                final String fieldName =
                                    Optional.ofNullable(datasetField.get(Facets.Dataset.FIELD_NAME))
                                        .map(String::valueOf)
                                        .orElseThrow(
                                            () ->
                                                new FacetNotValid.MissingNameForDatasetField(
                                                    dataset.getName()));

                                // Extract type for dataset field (optional).
                                final Optional<String> fieldType =
                                    Optional.ofNullable(datasetField.get(Facets.Dataset.FIELD_TYPE))
                                        .map(String::valueOf);

                                // Extract description for dataset field (optional).
                                final Optional<String> fieldDescription =
                                    Optional.ofNullable(
                                            datasetField.get(Facets.Dataset.FIELD_DESCRIPTION))
                                        .map(String::valueOf);

                                // Metadata.Dataset.Schema.Field instance (if present) with dataset
                                // field name (required).
                                final Metadata.Dataset.Schema.Field.FieldBuilder
                                    datasetFieldBuilder =
                                        Metadata.Dataset.Schema.Field.builder().name(fieldName);

                                // (optional)
                                fieldType.ifPresent(datasetFieldBuilder::type);
                                fieldDescription.ifPresent(datasetFieldBuilder::description);

                                return datasetFieldBuilder.build();
                              })
                          .collect(toImmutableSet()))
          .map(datasetFields -> Metadata.Dataset.Schema.builder().fields(datasetFields).build());
    }

    /**
     * Returns {@link Metadata.Dataset.Source} for the given {@link OpenLineage.Dataset} object.
     *
     * @see <a
     *     href="https://openlineage.io/spec/facets/1-0-1/DatasourceDatasetFacet.json">DatasourceDatasetFacet/a>
     */
    static Metadata.Dataset.Source sourceFor(@NonNull final OpenLineage.Dataset dataset) {
      return Optional.ofNullable(dataset.getFacets())
          .map(OpenLineage.DatasetFacets::getAdditionalProperties)
          .map(datasetFacets -> datasetFacets.get(Dataset.SOURCE))
          .map(OpenLineage.DatasetFacet::getAdditionalProperties)
          .map(
              datasetSourceFacet -> {
                // Extract name for dataset source (required).
                final SourceName sourceName =
                    Optional.ofNullable((String) datasetSourceFacet.get(Facets.Dataset.SOURCE_NAME))
                        .map(SourceName::of)
                        .orElseThrow(
                            () -> new FacetNotValid.MissingNameForDatasetSource(dataset.getName()));
                // Extract connection URI for dataset source (required).
                final URI connectionUrl =
                    Optional.ofNullable(
                            (String) datasetSourceFacet.get(Facets.Dataset.SOURCE_CONNECTION_URI))
                        .map(URI::create)
                        .orElseThrow(
                            () ->
                                new FacetNotValid.MissingConnectionUriForDatasetSource(
                                    dataset.getName()));
                // Metadata.Job.Source instance (if present) with source name and
                // connection URI for dataset (required).
                return Metadata.Dataset.Source.builder()
                    .name(sourceName)
                    .connectionUrl(connectionUrl)
                    .build();
              })
          .orElseThrow(() -> new FacetNotValid.MissingSourceForDataset(dataset.getName()));
    }
  }
}
