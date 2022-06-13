/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import marquez.client.models.Dataset;
import marquez.client.models.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("IntegrationTests")
public class TagIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
    createSource(STREAM_SOURCE_NAME);
  }

  @Test
  public void testApp_testTags() {
    client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);
    Dataset dataset =
        client.tagFieldWith(
            NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META.getFields().get(0).getName(), "TESTTAG");
    assertThat(dataset.getFields().get(0).getTags()).contains("TESTTAG");

    Dataset taggedDataset = client.tagDatasetWith(NAMESPACE_NAME, DB_TABLE_NAME, "TESTDATASETTAG");
    assertThat(taggedDataset.getTags()).contains("TESTDATASETTAG");
  }

  @Test
  public void testApp_testCreateTag() {
    Tag tag = client.createTag("tag", "Description");
    assertThat(tag.getName()).isEqualTo("tag");
    assertThat(tag.getDescription()).contains("Description");

    Tag tagWithoutDescription = client.createTag("tag2");
    assertThat(tagWithoutDescription.getName()).isEqualTo("tag2");
    assertThat(tagWithoutDescription.getDescription()).isEmpty();
  }

  @Test
  public void testApp_testUpsertTag() {
    Tag tag = client.createTag("tag", "Description");
    assertThat(tag.getName()).isEqualTo("tag");
    assertThat(tag.getDescription()).contains("Description");

    Tag tagWithoutDescription = client.createTag("tag", "New Description");

    assertThat(tagWithoutDescription.getName()).isEqualTo("tag");
    assertThat(tagWithoutDescription.getDescription()).contains("New Description");
  }

  @Test
  public void testApp_testFieldNotExists() {
    Assertions.assertThrows(
        Exception.class,
        () -> {
          client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);
          client.tagFieldWith(NAMESPACE_NAME, DB_TABLE_NAME, "not-exists", "TESTTAG");
        });
  }
}
