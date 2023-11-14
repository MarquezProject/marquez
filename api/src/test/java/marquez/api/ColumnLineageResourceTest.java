/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSortedSet;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import java.util.Map;
import marquez.common.Utils;
import marquez.service.ColumnLineageService;
import marquez.service.ServiceFactory;
import marquez.service.models.Lineage;
import marquez.service.models.Node;
import marquez.service.models.NodeId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ColumnLineageResourceTest {

  private static ResourceExtension UNDER_TEST;
  private static Lineage LINEAGE;

  static {
    ColumnLineageService lineageService = mock(ColumnLineageService.class);

    Node testNode =
        Utils.fromJson(
            ColumnLineageResourceTest.class.getResourceAsStream("/column_lineage/node.json"),
            new TypeReference<>() {});
    LINEAGE = new Lineage(ImmutableSortedSet.of(testNode));
    when(lineageService.lineage(any(NodeId.class), eq(20), eq(false))).thenReturn(LINEAGE);

    ServiceFactory serviceFactory =
        ApiTestUtils.mockServiceFactory(Map.of(ColumnLineageService.class, lineageService));

    UNDER_TEST =
        ResourceExtension.builder().addResource(new ColumnLineageResource(serviceFactory)).build();
  }

  @Test
  public void testGetColumnLineageByDatasetField() {
    final Lineage lineage =
        UNDER_TEST
            .target("/api/v1/column-lineage")
            .queryParam("nodeId", "datasetField:namespace:commonDataset:col_a")
            .request()
            .get()
            .readEntity(Lineage.class);

    assertEquals(lineage, LINEAGE);
  }

  @Test
  public void testGetColumnLineageByDataset() {
    final Lineage lineage =
        UNDER_TEST
            .target("/api/v1/column-lineage")
            .queryParam("nodeId", "dataset:namespace:commonDataset")
            .request()
            .get()
            .readEntity(Lineage.class);

    assertEquals(lineage, LINEAGE);
  }

  @Test
  public void testGetColumnLineageByVersionedNodeWithDownstream() {
    assertThat(
            UNDER_TEST
                .target("/api/v1/column-lineage")
                .queryParam(
                    "nodeId",
                    "dataset:namespace:commonDataset#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
                .queryParam("withDownstream", "true")
                .request()
                .get()
                .getStatus())
        .isEqualTo(400);
  }
}
