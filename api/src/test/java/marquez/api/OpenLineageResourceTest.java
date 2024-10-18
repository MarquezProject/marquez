/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSortedSet;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import java.util.Map;
import javax.ws.rs.core.Response;
import marquez.common.Utils;
import marquez.db.OpenLineageDao;
import marquez.service.JobService;
import marquez.service.LineageService;
import marquez.service.ServiceFactory;
import marquez.service.models.Lineage;
import marquez.service.models.Node;
import marquez.service.models.NodeId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
class OpenLineageResourceTest {
  private static ResourceExtension UNDER_TEST;
  private static Lineage LINEAGE;

  static {
    LineageService lineageService = mock(LineageService.class);
    OpenLineageDao openLineageDao = mock(OpenLineageDao.class);
    JobService jobService = mock(JobService.class);
    when(jobService.exists(anyString(), anyString())).thenReturn(true);

    Node testNode =
        Utils.fromJson(
            OpenLineageResourceTest.class.getResourceAsStream("/lineage/node.json"),
            new TypeReference<>() {});
    LINEAGE = new Lineage(ImmutableSortedSet.of(testNode));
    when(lineageService.lineage(any(NodeId.class), anyInt())).thenReturn(LINEAGE);

    ServiceFactory serviceFactory =
        ApiTestUtils.mockServiceFactory(
            Map.of(LineageService.class, lineageService, JobService.class, jobService));

    UNDER_TEST =
        ResourceExtension.builder()
            .addResource(new OpenLineageResource(serviceFactory, openLineageDao))
            .build();
  }

  @Test
  public void testGetLineage() {
    final Lineage lineage =
        UNDER_TEST
            .target("/api/v1/lineage")
            .queryParam("nodeId", "job:test-namespace:test-job")
            .request()
            .get()
            .readEntity(Lineage.class);

    assertEquals(lineage, LINEAGE);
  }

  @Test
  public void testGetLineageEventsBadSort() {
    final Response response =
        UNDER_TEST
            .target("/api/v1/events/lineage")
            .queryParam("sortDirection", "asdf")
            .request()
            .get();

    assertEquals(response.getStatus(), 400);
  }
}
