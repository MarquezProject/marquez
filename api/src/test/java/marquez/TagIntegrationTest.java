package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import marquez.client.models.Dataset;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTests.class)
public class TagIntegrationTest extends BaseIntegrationTest {

  @Before
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

  @Test(expected = Exception.class)
  public void testApp_testFieldNotExists() {
    client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);
    client.tagFieldWith(NAMESPACE_NAME, DB_TABLE_NAME, "not-exists", "TESTTAG");
  }
}
