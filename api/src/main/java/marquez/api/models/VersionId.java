package marquez.api.models;

import java.net.URL;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobId;
import marquez.common.models.JobVersionId;

public class VersionId {
  public static DatasetVersionId forDataset(
      @NonNull final DatasetId datasetId,
      @Nullable final Metadata.Dataset.Schema datasetSchema,
      @NonNull final Metadata.Dataset.Source source) {
    return DatasetVersionId.builder()
        .namespace(datasetId.getNamespace())
        .name(datasetId.getName())
        .version(UUID.randomUUID())
        .build();
  }

  public static JobVersionId forJob(
      @NonNull final JobId jobId, @Nullable final URL jobLocation, @Nullable final Metadata.IO io) {
    return JobVersionId.builder()
        .namespace(jobId.getNamespace())
        .name(jobId.getName())
        .version(UUID.randomUUID())
        .build();
  }
}
