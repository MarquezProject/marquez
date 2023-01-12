/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ColumnLineageNodeDataTest {

  @Test
  public void testGetters() {
    ColumnLineageNodeData node =
        new ColumnLineageNodeData(
            "namespace",
            "dataset",
            UUID.randomUUID(),
            "field",
            "varchar",
            ImmutableList.of(
                new InputFieldNodeData(
                    "namespace",
                    "dataset",
                    UUID.randomUUID(),
                    "other-field",
                    "transformation description",
                    "transformation type")));

    assertThat(node.getTransformationDescription()).isEqualTo("transformation description");
    assertThat(node.getTransformationType()).isEqualTo("transformation type");
  }

  @Test
  public void testGettersWhenEmptyInputFields() {
    ColumnLineageNodeData node =
        new ColumnLineageNodeData(
            "namespace", "dataset", UUID.randomUUID(), "field", "varchar", ImmutableList.of());
    assertThat(node.getTransformationDescription()).isNull();
    assertThat(node.getTransformationType()).isNull();
  }

  @Test
  public void testGettersWhenInputFieldsAreNull() {
    ColumnLineageNodeData node =
        new ColumnLineageNodeData(
            "namespace", "dataset", UUID.randomUUID(), "field", "varchar", null);
    assertThat(node.getTransformationDescription()).isNull();
    assertThat(node.getTransformationType()).isNull();
  }
}
