/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("IntegrationTests")
@Slf4j
public class MetricsIntegrationTest extends BaseIntegrationTest {
  @Test
  public void testCheckMetricV1Name() throws IOException {
    client.listNamespaces();
    CompletableFuture<String> response =
        this.getMetrics()
            .thenApply(HttpResponse::body)
            .whenComplete(
                (val, error) -> {
                  if (error != null) {
                    Assertions.fail("Could not complete request");
                  }
                });
    assertThat(response.join()).contains("marquez_db_NamespaceDao_findAll_count");
  }

  @Test
  public void testCheckMetricV2Name() throws IOException {
    client.listNamespaces();
    CompletableFuture<String> response =
        this.getMetricsV2()
            .thenApply(HttpResponse::body)
            .whenComplete(
                (val, error) -> {
                  if (error != null) {
                    Assertions.fail("Could not complete request");
                  }
                });
    assertThat(response.join())
        .contains(
            "marquez_sql_duration_seconds_sum{object_name=\"marquez.db.NamespaceDao\","
                + "method_name=\"findAll\","
                + "endpoint_method=\"GET\","
                + "endpoint_path=\"/api/v1/namespaces\",}");
  }
}
