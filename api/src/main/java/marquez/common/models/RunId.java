/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.Utils;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = RunId.FromValue.class)
@JsonSerialize(converter = RunId.ToValue.class)
public class RunId {
  @Getter private final UUID value;

  public RunId(final String valueAsString) {
    this(Utils.toUuid(valueAsString));
  }

  public RunId(@NonNull final UUID value) {
    this.value = value;
  }

  public static RunId of(final UUID value) {
    return new RunId(value);
  }

  public static class FromValue extends StdConverter<UUID, RunId> {
    @Override
    public RunId convert(UUID value) {
      return RunId.of(value);
    }
  }

  public static class ToValue extends StdConverter<RunId, UUID> {
    @Override
    public UUID convert(RunId value) {
      return value.getValue();
    }
  }
}
