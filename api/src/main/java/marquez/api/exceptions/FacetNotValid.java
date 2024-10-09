/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import java.io.Serial;
import java.util.UUID;
import javax.ws.rs.BadRequestException;

public class FacetNotValid {
  public static class MissingRunIdForParent extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingRunIdForParent(final UUID runId) {
      super(
          String.format(
              "Facet 'ParentRunFacet' for run '%s' not valid, missing 'runId' for parent run.",
              checkNotNull(runId)));
    }
  }

  public static class MissingJobForParent extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingJobForParent(final UUID runId) {
      super(
          String.format(
              "Facet 'ParentRunFacet' for run '%s' not valid, missing 'job' for parent run.",
              checkNotNull(runId)));
    }
  }

  public static class MissingNamespaceForParentJob extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingNamespaceForParentJob(final UUID runId) {
      super(
          String.format(
              "Facet 'ParentRunFacet' for run '%s' not valid, missing 'namespace' for parent job.",
              checkNotNull(runId)));
    }
  }

  public static class MissingNameForParentJob extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingNameForParentJob(final UUID runId) {
      super(
          String.format(
              "Facet 'ParentRunFacet' for run '%s' not valid, missing 'name' for parent job.",
              checkNotNull(runId)));
    }
  }

  public static class MissingNominalStartTimeForRun extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingNominalStartTimeForRun(final UUID runId) {
      super(
          String.format(
              "Facet 'NominalTimeRunFacet' for run '%s' not valid, missing 'nominalStartTime'.",
              checkNotNull(runId)));
    }
  }

  public static class MissingSourceCodeLanguageForJob extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingSourceCodeLanguageForJob(final String jobName) {
      super(
          String.format(
              "Facet 'SourceCodeJobFacet' for job '%s' not valid, missing 'language'.",
              checkNotBlank(jobName)));
    }
  }

  public static class MissingSourceCodeForJob extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingSourceCodeForJob(final String jobName) {
      super(
          String.format(
              "Facet 'SourceCodeJobFacet' for job '%s' not valid, missing 'sourceCode'.",
              checkNotBlank(jobName)));
    }
  }

  public static class MissingSourceCodeTypeForJob extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingSourceCodeTypeForJob(final String jobName) {
      super(
          String.format(
              "Facet 'SourceCodeLocationJobFacet' for job '%s' not valid, missing 'type'.",
              checkNotBlank(jobName)));
    }
  }

  public static class MissingSourceCodeUrlForJob extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingSourceCodeUrlForJob(final String jobName) {
      super(
          String.format(
              "Facet 'SourceCodeLocationJobFacet' for job '%s' not valid, missing 'url'.",
              checkNotBlank(jobName)));
    }
  }

  public static class MissingDescriptionForJob extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingDescriptionForJob(final String jobName) {
      super(
          String.format(
              "Facet 'DocumentationJobFacet' for job '%s' not valid, missing 'description'.",
              checkNotBlank(jobName)));
    }
  }

  public static class MissingNameForDatasetField extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingNameForDatasetField(final String datasetName) {
      super(
          String.format(
              "Facet 'SchemaDatasetFacet' for dataset '%s' not valid, missing 'name'.",
              checkNotBlank(datasetName)));
    }
  }

  public static class MissingNameForDatasetSource extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingNameForDatasetSource(final String datasetName) {
      super(
          String.format(
              "Facet 'DatasourceDatasetFacet' for dataset '%s' not valid, missing 'name' for source.",
              checkNotBlank(datasetName)));
    }
  }

  public static class MissingConnectionUriForDatasetSource extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingConnectionUriForDatasetSource(final String datasetName) {
      super(
          String.format(
              "Facet 'DatasourceDatasetFacet' for dataset '%s' not valid, missing 'uri' for source.",
              checkNotBlank(datasetName)));
    }
  }

  public static class MissingSourceForDataset extends BadRequestException {
    @Serial private static final long serialVersionUID = 1L;

    public MissingSourceForDataset(final String datasetName) {
      super(
          String.format(
              "Dataset '%s' not valid, missing 'DatasourceDatasetFacet'.",
              checkNotBlank(datasetName)));
    }
  }
}
