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
@JsonDeserialize(converter = SourceType.FromValue.class)
@JsonSerialize(converter = SourceType.ToValue.class)
public final class SourceType {
  @Getter private final String value;

  public SourceType(@NonNull final String value) {
    this.value = checkNotBlank(value, "value must not be blank");
  }

  public static SourceType of(final String value) {
    return new SourceType(value);
  }

  public static class FromValue extends StdConverter<String, SourceType> {
    @Override
    public SourceType convert(@NonNull String value) {
      return SourceType.of(value);
    }
  }

  public static class ToValue extends StdConverter<SourceType, String> {
    @Override
    public String convert(@NonNull SourceType type) {
      return type.getValue();
    }
  }
}
