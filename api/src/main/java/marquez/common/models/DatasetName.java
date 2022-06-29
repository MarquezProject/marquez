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
@JsonDeserialize(converter = DatasetName.FromValue.class)
@JsonSerialize(converter = DatasetName.ToValue.class)
public final class DatasetName {
  @Getter private final String value;

  public DatasetName(@NonNull final String value) {
    this.value = checkNotBlank(value, "value must not be blank");
  }

  public static DatasetName of(final String value) {
    return new DatasetName(value);
  }

  public static class FromValue extends StdConverter<String, DatasetName> {
    @Override
    public DatasetName convert(@NonNull String value) {
      return DatasetName.of(value);
    }
  }

  public static class ToValue extends StdConverter<DatasetName, String> {
    @Override
    public String convert(@NonNull DatasetName name) {
      return name.getValue();
    }
  }
}
