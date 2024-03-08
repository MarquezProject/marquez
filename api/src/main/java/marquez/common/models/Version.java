/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.joining;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.Utils;
import marquez.common.models.Version.UUIDToVersion;
import marquez.common.models.Version.VersionToUUID;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = UUIDToVersion.class)
@JsonSerialize(converter = VersionToUUID.class)
public class Version {
  public static final String KV_DELIM = "#";
  public static final Joiner.MapJoiner KV_JOINER = Joiner.on(KV_DELIM).withKeyValueSeparator("=");
  public static final String VERSION_DELIM = ":";
  public static final Joiner VERSION_JOINER = Joiner.on(VERSION_DELIM).skipNulls();

  @Getter private final UUID value;

  public Version(final String value) {
    this(Utils.toUuid(checkNotBlank(value)));
  }

  public Version(final UUID value) {
    checkNotNull(value, "UUID must not be null");
    checkNotBlank(value.toString(), "UUID value must not be blank");
    this.value = value;
  }

  public static Version of(final UUID value) {
    return new Version(value);
  }

  public static class VersionToUUID extends StdConverter<Version, UUID> {
    @Override
    public UUID convert(Version value) {
      return value.getValue();
    }
  }

  public static class UUIDToVersion extends StdConverter<UUID, Version> {
    @Override
    public Version convert(UUID value) {
      return Version.of(value);
    }
  }

  public static Version forJob(
      @NonNull final NamespaceName namespaceName,
      @NonNull final JobName jobName,
      @NonNull final ImmutableSet<DatasetVersionId> jobInputIds,
      @NonNull final ImmutableSet<DatasetVersionId> jobOutputIds,
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
                jobLocation)
            .getBytes(UTF_8);
    return Version.of(UUID.nameUUIDFromBytes(bytes));
  }
}
