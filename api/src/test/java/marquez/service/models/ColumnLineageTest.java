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
            .build();

    assertThat(columnLineage.getTransformationDescription())
        .isEqualTo("transformation description");
    assertThat(columnLineage.getTransformationType()).isEqualTo("transformation type");
  }

  @Test
  public void testGettersWhenEmptyInputFields() {
    ColumnLineage columnLineage =
        ColumnLineage.builder().name("name").inputFields(ImmutableList.of()).build();
    assertThat(columnLineage.getTransformationDescription()).isNull();
    assertThat(columnLineage.getTransformationType()).isNull();
  }

  @Test
  public void testGettersWhenInputFieldsAreNull() {
    ColumnLineage columnLineage = ColumnLineage.builder().name("name").inputFields(null).build();
    assertThat(columnLineage.getTransformationDescription()).isNull();
    assertThat(columnLineage.getTransformationType()).isNull();
  }
}
