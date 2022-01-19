/* SPDX-License-Identifier: Apache-2.0 */

package marquez.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSortedSet;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import java.util.Map;
import marquez.common.Utils;
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

  static {
    LineageService lineageService = mock(LineageService.class);

    Node testNode =
        Utils.fromJson(
            OpenLineageResourceTest.class.getResourceAsStream("/lineage/node.json"),
            new TypeReference<>() {});

    when(lineageService.lineage(any(NodeId.class), anyInt()))
        .thenReturn(new Lineage(ImmutableSortedSet.of(testNode)));

    ServiceFactory serviceFactory =
        ApiTestUtils.mockServiceFactory(Map.of(LineageService.class, lineageService));

    UNDER_TEST =
        ResourceExtension.builder()
            .addResource(new OpenLineageResource(serviceFactory))
            .addResource(new LineageResource(serviceFactory))
            .build();
  }

  @Test
  void testEqualResponseFromV1AndV1Beta() {
    Lineage responseBeta =
        UNDER_TEST
            .target("/api/v1-beta/lineage")
            .queryParam("nodeId", "job:test")
            .request()
            .get()
            .readEntity(Lineage.class);
    Lineage response =
        UNDER_TEST
            .target("/api/v1/lineage")
            .queryParam("nodeId", "job:test")
            .request()
            .get()
            .readEntity(Lineage.class);

    assertEquals(response, responseBeta);
  }
}
