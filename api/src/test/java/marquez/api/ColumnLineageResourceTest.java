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
import jakarta.ws.rs.core.Response;
import java.util.Map;
import marquez.common.Utils;
import marquez.service.ColumnLineageService;
import marquez.service.ServiceFactory;
import marquez.service.exceptions.NodeIdNotFoundException;
import marquez.service.models.Lineage;
import marquez.service.models.Node;
import marquez.service.models.NodeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ColumnLineageResourceTest {

  private static final ColumnLineageService lineageService = mock(ColumnLineageService.class);
  private static final Lineage LINEAGE;
  private static final ResourceExtension UNDER_TEST;

  static {
    Node testNode =
        Utils.fromJson(
            ColumnLineageResourceTest.class.getResourceAsStream("/column_lineage/node.json"),
            new TypeReference<>() {});
    LINEAGE = new Lineage(ImmutableSortedSet.of(testNode));
    ServiceFactory serviceFactory =
        ApiTestUtils.mockServiceFactory(Map.of(ColumnLineageService.class, lineageService));
    UNDER_TEST =
        ResourceExtension.builder().addResource(new ColumnLineageResource(serviceFactory)).build();
  }

  @BeforeEach
  public void setup() {
    Mockito.reset(lineageService);
    // Default behavior for most tests
    when(lineageService.lineage(any(NodeId.class), eq(20), eq(false))).thenReturn(LINEAGE);
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

  @Test
  public void testGetColumnLineageWithMissingNodeId() {
    Response response = UNDER_TEST
        .target("/api/v1/column-lineage")
        .request()
        .get();
    
    assertThat(response.getStatus()).isEqualTo(400);
    Map<String, String> error = response.readEntity(Map.class);
    assertThat(error.get("error")).isEqualTo("Missing required query param: nodeId");
  }

  @Test
  public void testGetColumnLineageWithBlankNodeId() {
    Response response = UNDER_TEST
        .target("/api/v1/column-lineage")
        .queryParam("nodeId", "   ")
        .request()
        .get();
    
    assertThat(response.getStatus()).isEqualTo(400);
    Map<String, String> error = response.readEntity(Map.class);
    assertThat(error.get("error")).isEqualTo("Missing required query param: nodeId");
  }

  @Test
  public void testGetColumnLineageWithInvalidNodeId() {
    Response response = UNDER_TEST
        .target("/api/v1/column-lineage")
        .queryParam("nodeId", "invalid:format")
        .request()
        .get();
    
    assertThat(response.getStatus()).isEqualTo(400);
    Map<String, String> error = response.readEntity(Map.class);
    assertThat(error.get("error")).isEqualTo("Invalid nodeId format");
  }

  @Test
  public void testGetColumnLineageWithNodeNotFound() {
    // Mock the service to throw NodeIdNotFoundException
    when(lineageService.lineage(any(NodeId.class), eq(20), eq(false)))
        .thenThrow(new NodeIdNotFoundException("Node not found"));

    Response response = UNDER_TEST
        .target("/api/v1/column-lineage")
        .queryParam("nodeId", "dataset:namespace:nonExistentDataset")
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(404);
    Map<String, String> error = response.readEntity(Map.class);
    assertThat(error.get("error")).isEqualTo("Node not found");
  }

  @Test
  public void testGetColumnLineageWithCustomDepth() {
    // Mock the service to return lineage with custom depth
    when(lineageService.lineage(any(NodeId.class), eq(5), eq(false))).thenReturn(LINEAGE);

    final Lineage lineage = UNDER_TEST
        .target("/api/v1/column-lineage")
        .queryParam("nodeId", "dataset:namespace:commonDataset")
        .queryParam("depth", "5")
        .request()
        .get()
        .readEntity(Lineage.class);

    assertEquals(lineage, LINEAGE);
  }

  @Test
  public void testGetColumnLineageWithDownstreamForNonVersionedNode() {
    // Mock the service to return lineage with downstream
    when(lineageService.lineage(any(NodeId.class), eq(20), eq(true))).thenReturn(LINEAGE);

    final Lineage lineage = UNDER_TEST
        .target("/api/v1/column-lineage")
        .queryParam("nodeId", "dataset:namespace:commonDataset")
        .queryParam("withDownstream", "true")
        .request()
        .get()
        .readEntity(Lineage.class);

    assertEquals(lineage, LINEAGE);
  }

  @Test
  public void testGetColumnLineageWithInternalServerError() {
    // Mock the service to throw a general exception
    when(lineageService.lineage(any(NodeId.class), eq(20), eq(false)))
        .thenThrow(new RuntimeException("Internal error"));

    Response response = UNDER_TEST
        .target("/api/v1/column-lineage")
        .queryParam("nodeId", "dataset:namespace:commonDataset")
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(500);
    Map<String, String> error = response.readEntity(Map.class);
    assertThat(error.get("error")).isEqualTo("Internal server error");
  }
}
