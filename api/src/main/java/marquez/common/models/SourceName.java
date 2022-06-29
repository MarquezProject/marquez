/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = SourceName.FromValue.class)
@JsonSerialize(converter = SourceName.ToValue.class)
public final class SourceName {
  @Getter private final String value;

  public SourceName(@NonNull final String value) {
    this.value = checkNotBlank(value, "value must not be blank");
  }

  public static SourceName of(final String value) {
    return new SourceName(value);
  }

  public static class FromValue extends StdConverter<String, SourceName> {
    @Override
    public SourceName convert(@NonNull String value) {
      return SourceName.of(value);
    }
  }

  public static class ToValue extends StdConverter<SourceName, String> {
    @Override
    public String convert(@NonNull SourceName name) {
      return name.getValue();
    }
  }
}
