/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import lombok.Value;

@Value
public class LineageEvent {
  String eventType;
  ZonedDateTime eventTime;
  Map<String, Object> run;
  Map<String, Object> job;
  List<Object> inputs;
  List<Object> outputs;
  URI producer;
  URI schemaURL;

  public URI getSchemaURL() {
    if (schemaURL == null) {
      return URI.create("https://openlineage.io/spec/2-0-0/OpenLineage.json#/definitions/RunEvent");
    }
    return schemaURL;
  }
}
