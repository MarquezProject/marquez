/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.ColumnLineageInputField;
import marquez.service.models.LineageEvent.ColumnLineageInputField.Transformation;
import marquez.service.models.LineageEvent.ColumnLineageOutputColumn;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for https://github.com/MarquezProject/marquez/issues/3100: transformation
 * details reported via the (non-deprecated) {@code InputField.transformations[]} array of the
 * OpenLineage {@code ColumnLineageDatasetFacet} were always dropped, so {@code
 * transformation_description}/{@code transformation_type} always ended up null in Marquez,
 * regardless of what the producer actually reported.
 *
 * <p>These tests exercise {@link OpenLineageDao#transformationOf(ColumnLineageInputField,
 * ColumnLineageOutputColumn)} directly - the pure function responsible for resolving which
 * transformation applies to a given input field - so they can run without a database.
 */
@org.junit.jupiter.api.Tag("UnitTests")
class OpenLineageDaoColumnLineageTransformationTest {

  @Test
  void prefersPerInputFieldTransformationOverDeprecatedOutputColumnFields() {
    // Reproduces the exact scenario from the issue: the output column itself does not set the
    // deprecated transformationDescription/transformationType, but each input field reports its
    // own transformations[] entry.
    ColumnLineageInputField inputField =
        ColumnLineageInputField.builder()
            .namespace("bookstore2")
            .name("customers")
            .field("customer_email")
            .transformations(
                Collections.singletonList(
                    Transformation.builder()
                        .type("DIRECT")
                        .subtype("TRANSFORMATION")
                        .description("concat(customers.customer_name, ' - ', customers.customer_email)")
                        .build()))
            .build();
    ColumnLineageOutputColumn outputColumn =
        ColumnLineageOutputColumn.builder().inputFields(List.of(inputField)).build();

    Pair<String, String> transformation = OpenLineageDao.transformationOf(inputField, outputColumn);

    assertThat(transformation.getLeft())
        .isEqualTo("concat(customers.customer_name, ' - ', customers.customer_email)");
    assertThat(transformation.getRight()).isEqualTo("DIRECT");
  }

  @Test
  void distinctInputFieldsOnTheSameOutputColumnCanHaveDifferentTransformations() {
    // customer_full in the issue is fed by two input fields, each with a different underlying
    // source field but (in the issue's example) the same description; verify the resolution is
    // genuinely per-input-field rather than picking a single value for the whole output column.
    ColumnLineageInputField emailField =
        ColumnLineageInputField.builder()
            .namespace("bookstore2")
            .name("customers")
            .field("customer_email")
            .transformations(
                Collections.singletonList(
                    Transformation.builder().type("DIRECT").description("descriptionA").build()))
            .build();
    ColumnLineageInputField nameField =
        ColumnLineageInputField.builder()
            .namespace("bookstore2")
            .name("customers")
            .field("customer_name")
            .transformations(
                Collections.singletonList(
                    Transformation.builder().type("INDIRECT").description("descriptionB").build()))
            .build();
    ColumnLineageOutputColumn outputColumn =
        ColumnLineageOutputColumn.builder()
            .inputFields(Arrays.asList(emailField, nameField))
            .build();

    Pair<String, String> emailTransformation =
        OpenLineageDao.transformationOf(emailField, outputColumn);
    Pair<String, String> nameTransformation =
        OpenLineageDao.transformationOf(nameField, outputColumn);

    assertThat(emailTransformation).isEqualTo(Pair.of("descriptionA", "DIRECT"));
    assertThat(nameTransformation).isEqualTo(Pair.of("descriptionB", "INDIRECT"));
  }

  @Test
  void fallsBackToDeprecatedOutputColumnFieldsWhenInputFieldHasNoTransformations() {
    // Producers using the old (deprecated) format never set inputField.transformations at all;
    // this must keep working exactly as before.
    ColumnLineageInputField inputField =
        ColumnLineageInputField.builder()
            .namespace("ns")
            .name("upstream")
            .field("col")
            .build(); // no transformations set
    ColumnLineageOutputColumn outputColumn =
        ColumnLineageOutputColumn.builder()
            .inputFields(List.of(inputField))
            .transformationDescription("legacy description")
            .transformationType("IDENTITY")
            .build();

    Pair<String, String> transformation = OpenLineageDao.transformationOf(inputField, outputColumn);

    assertThat(transformation).isEqualTo(Pair.of("legacy description", "IDENTITY"));
  }

  @Test
  void returnsNullPairWhenNeitherFormatIsReported() {
    ColumnLineageInputField inputField =
        ColumnLineageInputField.builder().namespace("ns").name("upstream").field("col").build();
    ColumnLineageOutputColumn outputColumn =
        ColumnLineageOutputColumn.builder().inputFields(List.of(inputField)).build();

    Pair<String, String> transformation = OpenLineageDao.transformationOf(inputField, outputColumn);

    assertThat(transformation.getLeft()).isNull();
    assertThat(transformation.getRight()).isNull();
  }

  @Test
  void usesOnlyFirstTransformationWhenMultipleAreReportedForTheSameInputField() {
    ColumnLineageInputField inputField =
        ColumnLineageInputField.builder()
            .namespace("ns")
            .name("upstream")
            .field("col")
            .transformations(
                Arrays.asList(
                    Transformation.builder().type("DIRECT").description("first").build(),
                    Transformation.builder().type("INDIRECT").description("second").build()))
            .build();
    ColumnLineageOutputColumn outputColumn =
        ColumnLineageOutputColumn.builder().inputFields(List.of(inputField)).build();

    Pair<String, String> transformation = OpenLineageDao.transformationOf(inputField, outputColumn);

    assertThat(transformation).isEqualTo(Pair.of("first", "DIRECT"));
  }
}
