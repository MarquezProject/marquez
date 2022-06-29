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
@JsonDeserialize(converter = OwnerName.FromValue.class)
@JsonSerialize(converter = OwnerName.ToValue.class)
public final class OwnerName {
  @Getter private final String value;

  public OwnerName(@NonNull final String value) {
    this.value = checkNotBlank(value, "value must not be blank");
  }

  public static OwnerName of(final String value) {
    return new OwnerName(value);
  }

  public static class FromValue extends StdConverter<String, OwnerName> {
    @Override
    public OwnerName convert(@NonNull String value) {
      return OwnerName.of(value);
    }
  }

  public static class ToValue extends StdConverter<OwnerName, String> {
    @Override
    public String convert(@NonNull OwnerName name) {
      return name.getValue();
    }
  }

  public static final OwnerName ANONYMOUS = OwnerName.of("anonymous");
}
