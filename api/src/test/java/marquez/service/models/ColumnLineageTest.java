/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

public class ColumnLineageTest {

  @Test
  public void testGetters() {
    ColumnLineage columnLineage =
        ColumnLineage.builder()
            .name("name")
            .inputFields(
                ImmutableList.of(
                    new ColumnLineageInputField(
                        "namespace",
                        "dataset",
                        "other-field",
                        "transformation description",
                        "transformation type")))
            .outputFields(ImmutableList.of())
            .build();

    assertThat(columnLineage.getTransformationDescription())
        .isEqualTo("transformation description");
    assertThat(columnLineage.getTransformationType()).isEqualTo("transformation type");
  }

  @Test
  public void testGettersWhenEmptyInputFields() {
    ColumnLineage columnLineage =
        ColumnLineage.builder()
            .name("name")
            .inputFields(ImmutableList.of())
            .outputFields(ImmutableList.of())
            .build();
    assertThat(columnLineage.getTransformationDescription()).isNull();
    assertThat(columnLineage.getTransformationType()).isNull();
  }

  @Test
  public void testGettersWithMultipleInputFields() {
    ColumnLineage columnLineage =
        ColumnLineage.builder()
            .name("name")
            .inputFields(
                ImmutableList.of(
                    new ColumnLineageInputField(
                        "namespace1",
                        "dataset1",
                        "field1",
                        "transformation description 1",
                        "transformation type 1"),
                    new ColumnLineageInputField(
                        "namespace2",
                        "dataset2",
                        "field2",
                        "transformation description 2",
                        "transformation type 2")))
            .outputFields(ImmutableList.of())
            .build();
    // Should return the first input field's transformation values
    assertThat(columnLineage.getTransformationDescription())
        .isEqualTo("transformation description 1");
    assertThat(columnLineage.getTransformationType()).isEqualTo("transformation type 1");
  }
}
