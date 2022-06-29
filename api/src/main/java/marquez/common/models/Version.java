/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import static com.google.common.base.Preconditions.checkNotNull;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import marquez.common.Utils;
import marquez.common.models.Version.UUIDToVersion;
import marquez.common.models.Version.VersionToUUID;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = UUIDToVersion.class)
@JsonSerialize(converter = VersionToUUID.class)
public class Version {
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
}
