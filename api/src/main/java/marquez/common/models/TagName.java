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
@JsonDeserialize(converter = TagName.FromValue.class)
@JsonSerialize(converter = TagName.ToValue.class)
public final class TagName {
  @Getter private final String value;

  public TagName(@NonNull final String value) {
    this.value = checkNotBlank(value, "value must not be blank");
  }

  public static TagName of(final String value) {
    return new TagName(value);
  }

  public static class FromValue extends StdConverter<String, TagName> {
    @Override
    public TagName convert(@NonNull String value) {
      return TagName.of(value);
    }
  }

  public static class ToValue extends StdConverter<TagName, String> {
    @Override
    public String convert(@NonNull TagName name) {
      return name.getValue();
    }
  }
}
