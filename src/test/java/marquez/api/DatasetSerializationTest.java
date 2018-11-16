package marquez.api;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Ignore;


@Ignore("TODO: Need to figure out why this started to fail. Please see issue #140")
public class DatasetSerializationTest {

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final String DATASET_SAMPLE_JSON = "fixtures/dataset.json";
  private static Dataset DATASET;

  @BeforeClass
  public static void setupDataset() {
    SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
    try {
      Timestamp createdAt = new Timestamp(dateFmt.parse("2018-07-14 19:43:37+0000").getTime());
      Timestamp updatedAt = new Timestamp(dateFmt.parse("2018-08-15 11:20:05+0000").getTime());

      DATASET =
          new Dataset(
              "sample_dataset",
              createdAt,
              updatedAt,
              Dataset.Type.DB_TABLE,
              Dataset.Origin.EXTERNAL,
              UUID.fromString("10892965-454c-4bb1-b187-d67f2141423c"),
              "sample dataset for testing");
    } catch (ParseException e) {
      fail("couldn't parse test timestamps");
    }
  }

  @Ignore @Test
  public void serializesToJSON() throws Exception {
    final String expected =
        MAPPER.writeValueAsString(MAPPER.readValue(fixture(DATASET_SAMPLE_JSON), Dataset.class));
    assertThat(MAPPER.writeValueAsString(DATASET)).isEqualTo(expected);
  }

  @Ignore @Test
  public void deserializesFromJSON() throws Exception {
    assertThat(MAPPER.readValue(fixture(DATASET_SAMPLE_JSON), Dataset.class)).isEqualTo(DATASET);
  }
}
