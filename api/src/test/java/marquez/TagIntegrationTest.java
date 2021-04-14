package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import marquez.client.models.Dataset;
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
  public void testApp_testFieldNotExists() {
    Assertions.assertThrows(
        Exception.class,
        () -> {
          client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);
          client.tagFieldWith(NAMESPACE_NAME, DB_TABLE_NAME, "not-exists", "TESTTAG");
        });
  }
}
