/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class ColumnLineageInputFieldTest {
  @Test
  void testToleratesNullTransformationTypeAndDescription() {
    ColumnLineageInputField field = new ColumnLineageInputField("ns", "ds", "field", null, null);

    assertEquals("ns", field.getNamespace());
    assertEquals("ds", field.getDataset());
    assertEquals("field", field.getField());
    assertNull(field.getTransformationDescription());
    assertNull(field.getTransformationType());
  }
}
