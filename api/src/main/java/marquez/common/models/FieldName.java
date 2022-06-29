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
@JsonDeserialize(converter = FieldName.FromValue.class)
@JsonSerialize(converter = FieldName.ToValue.class)
public final class FieldName {
  @Getter private final String value;

  public FieldName(@NonNull final String value) {
    this.value = checkNotBlank(value, "value must not be blank");
  }

  public static FieldName of(final String value) {
    return new FieldName(value);
  }

  public static class FromValue extends StdConverter<String, FieldName> {
    @Override
    public FieldName convert(@NonNull String value) {
      return FieldName.of(value);
    }
  }

  public static class ToValue extends StdConverter<FieldName, String> {
    @Override
    public String convert(@NonNull FieldName name) {
      return name.getValue();
    }
  }
}
