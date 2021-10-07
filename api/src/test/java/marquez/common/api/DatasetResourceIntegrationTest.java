package marquez.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import marquez.BaseIntegrationTest;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class DatasetResourceIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
  }

  @Test
  public void testApp_getJobVersionWithInputsAndOutputs() {
    int min = 10;
    int max = 25;
    Random rand = new Random();
    int datasetCount = rand.nextInt((max - min) + 1) + min;

    for (int i = 0; i < datasetCount; i++) {
      String generatedString = RandomStringUtils.randomAlphanumeric(10);
      client.createDataset(NAMESPACE_NAME, generatedString, DB_TABLE_META);
    }
    assertThat(client.countDatasets(NAMESPACE_NAME)).isEqualTo(datasetCount);
  }
}
