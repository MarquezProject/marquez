package marquez.api.models;

import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobVersionId;
import marquez.common.models.Version;

import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class VersionId {
  public static JobVersionId forJob() {
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
                            jobLocation)
                    .getBytes(UTF_8);
    return JobVersionId.of(UUID.nameUUIDFromBytes(bytes));
  }

  public static DatasetVersionId forDataset() {
    return null;
  }
}
