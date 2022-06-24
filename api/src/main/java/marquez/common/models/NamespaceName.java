/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = NamespaceName.FromValue.class)
@JsonSerialize(converter = NamespaceName.ToValue.class)
public final class NamespaceName {
  private static final int MIN_SIZE = 1;
  private static final int MAX_SIZE = 1024;
  private static final Pattern PATTERN =
      Pattern.compile(String.format("^[a-zA-Z:;=/0-9_\\-\\.@+]{%d,%d}$", MIN_SIZE, MAX_SIZE));

  @Getter private final String value;

  public NamespaceName(@NonNull final String value) {
    checkArgument(
        PATTERN.matcher(value).matches(),
        "namespace '%s' must contain only letters (a-z, A-Z), numbers (0-9), "
            + "underscores (_), at (@), plus (+), dashes (-), colons (:), equals (=), semicolons (;), slashes (/) "
            + "or dots (.) with a maximum length of %s characters.",
        value,
        MAX_SIZE);
    this.value = value;
  }

  public static NamespaceName of(final String value) {
    return new NamespaceName(value);
  }

  public static class FromValue extends StdConverter<String, NamespaceName> {
    @Override
    public NamespaceName convert(@NonNull String value) {
      return NamespaceName.of(value);
    }
  }

  public static class ToValue extends StdConverter<NamespaceName, String> {
    @Override
    public String convert(@NonNull NamespaceName name) {
      return name.getValue();
    }
  }

  public static final NamespaceName DEFAULT = NamespaceName.of("default");
}
