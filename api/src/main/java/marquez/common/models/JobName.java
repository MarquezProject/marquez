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
@JsonDeserialize(converter = JobName.FromValue.class)
@JsonSerialize(converter = JobName.ToValue.class)
public final class JobName {
  @Getter private final String value;

  public JobName(@NonNull final String value) {
    this.value = checkNotBlank(value, "value must not be blank");
  }

  public static JobName of(final String value) {
    return new JobName(value);
  }

  public static class FromValue extends StdConverter<String, JobName> {
    @Override
    public JobName convert(@NonNull String value) {
      return JobName.of(value);
    }
  }

  public static class ToValue extends StdConverter<JobName, String> {
    @Override
    public String convert(@NonNull JobName name) {
      return name.getValue();
    }
  }
}
