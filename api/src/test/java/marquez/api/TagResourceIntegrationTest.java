/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newTagName;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import marquez.client.models.Dataset;
import marquez.client.models.Tag;
import org.junit.jupiter.api.Test;

public class TagResourceIntegrationTest extends BaseResourceIntegrationTest {

  @Test
  public void testApp_createTag() {
    // (1) List tags.
    final Set<Tag> tagsBefore = MARQUEZ_CLIENT.listTags();

    // (2) Add new tag.
    final Tag newTag = MARQUEZ_CLIENT.createTag(newTagName().getValue(), newDescription());

    // (3) List tags; ensure new tag has been added.
    final Set<Tag> tagsAfter = MARQUEZ_CLIENT.listTags();
    assertThat(tagsAfter).hasSize(tagsBefore.size() + 1);
    assertThat(tagsAfter).contains(newTag);
  }

  @Test
  public void testApp_listTags() {
    // (1) List tags.
    final Set<Tag> tags = MARQUEZ_CLIENT.listTags();

    // (2) Ensure tags 'PII', 'SENSITIVE' defined in 'config.test.yml' are present.
    assertThat(tags).contains(PII, SENSITIVE);
  }

  @Test
  public void testApp_testDatasetTagDelete() {
    // Create Namespace
    createNamespace(NAMESPACE_NAME);
    // create a source
    createSource(DB_TABLE_SOURCE_NAME);
    // Create Dataset
    MARQUEZ_CLIENT.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);

    // Tag Dataset with TESTDATASETTAG tag
    Dataset taggedDataset =
        MARQUEZ_CLIENT.tagDatasetWith(NAMESPACE_NAME, DB_TABLE_NAME, "TESTDATASETTAG");
    assertThat(taggedDataset.getTags()).contains("TESTDATASETTAG");

    // Test that the tag TESTDATASETTAG is deleted from the dataset
    Dataset taggedDeleteDataset =
        MARQUEZ_CLIENT.deleteDatasetTag(NAMESPACE_NAME, DB_TABLE_NAME, "TESTDATASETTAG");
    assertThat(taggedDeleteDataset.getTags()).doesNotContain("TESTDATASETTAG");
    // assert the number of tags should be 1
    assertThat(taggedDeleteDataset.getTags()).hasSize(1);
    // assert that only PII remains
    assertThat(taggedDeleteDataset.getTags()).containsExactly("PII");
  }

  @Test
  public void testApp_testDatasetTagFieldDelete() {
    // Create Namespace
    createNamespace(NAMESPACE_NAME);
    // create a source
    createSource(DB_TABLE_SOURCE_NAME);
    // Create Dataset
    MARQUEZ_CLIENT.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);

    // tag dataset field
    Dataset taggedDatasetField =
        MARQUEZ_CLIENT.tagFieldWith(
            NAMESPACE_NAME,
            DB_TABLE_NAME,
            DB_TABLE_META.getFields().get(0).getName(),
            "TESTFIELDTAG");
    // assert the tag TESTFIELDTAG has been added to field at position 0
    assertThat(taggedDatasetField.getFields().get(0).getTags()).contains("TESTFIELDTAG");
    // assert a total of two tags exist on the field
    assertThat(taggedDatasetField.getFields().get(0).getTags()).hasSize(2);

    // delete the field tag TESTFIELDTAG from the dataset field at position 0
    Dataset taggedDatasetFieldDelete =
        MARQUEZ_CLIENT.deleteDatasetFieldTag(
            NAMESPACE_NAME,
            DB_TABLE_NAME,
            DB_TABLE_META.getFields().get(0).getName(),
            "TESTFIELDTAG");

    // Test that the tag TESTDATASETTAG is deleted from the dataset field at position 0
    assertThat(taggedDatasetFieldDelete.getFields().get(0).getTags())
        .doesNotContain("TESTFIELDTAG");
    // assert the number of tags on the field should be 1
    assertThat(taggedDatasetFieldDelete.getFields().get(0).getTags()).hasSize(1);
    // assert that only SENSITIVE remains
    assertThat(taggedDatasetFieldDelete.getFields().get(0).getTags()).containsExactly("SENSITIVE");
  }
}
