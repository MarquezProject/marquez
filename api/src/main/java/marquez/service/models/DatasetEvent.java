/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import java.net.URI;
import java.time.ZonedDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Valid
@ToString
public class DatasetEvent extends BaseEvent {
  @NotNull private ZonedDateTime eventTime;
  @Valid private LineageEvent.Dataset dataset;
  @Valid @NotNull private String producer;
  @Valid @NotNull private URI schemaURL;
}
