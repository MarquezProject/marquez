/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class LineageEventTest {

  public static final URI VALID_SCHEMA_URL =
      URI.create("https://openlineage.io/spec/2-5-0/OpenLineage.json#/definitions/RunEvent");

  @Test
  public void testGetSchemaURL() {
    assertThat(createlLineageEventWith(VALID_SCHEMA_URL).getSchemaURL())
        .isEqualTo(VALID_SCHEMA_URL);
  }

  @Test
  public void testGetSchemaURLWhenNull() {
    assertThat(createlLineageEventWith(null).getSchemaURL())
        .isEqualTo(
            URI.create("https://openlineage.io/spec/2-0-0/OpenLineage.json#/definitions/RunEvent"));
  }

  private LineageEvent createlLineageEventWith(URI schemaURL) {
    return new LineageEvent(
        "START",
        ZonedDateTime.now(ZoneId.of("UTC")),
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyList(),
        Collections.emptyList(),
        URI.create("http://localhost:8080"),
        schemaURL);
  }
}
