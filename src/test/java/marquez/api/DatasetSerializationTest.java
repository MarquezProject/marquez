package marquez.api;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;

public class DatasetSerializationTest {

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final String DATASET_SAMPLE_JSON = "fixtures/dataset.json";
  private static Dataset DATASET;

  @BeforeClass
  public static void setupDataset() {
    TimeZone tz = TimeZone.getDefault();
    Calendar cal = GregorianCalendar.getInstance(tz);
    int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

    Timestamp createdAt =
        new Timestamp(Timestamp.valueOf("2018-07-14 19:43:37").getTime() + offsetInMillis);
    Timestamp updatedAt =
        new Timestamp(Timestamp.valueOf("2018-08-15 11:20:05").getTime() + offsetInMillis);

    DATASET =
        new Dataset(
            "sample_dataset",
            createdAt,
            updatedAt,
            Dataset.Type.DB_TABLE,
            Dataset.Origin.EXTERNAL,
            UUID.fromString("10892965-454c-4bb1-b187-d67f2141423c"),
            "sample dataset for testing");
  }

  @Test
  public void serializesToJSON() throws Exception {
    final String expected =
        MAPPER.writeValueAsString(MAPPER.readValue(fixture(DATASET_SAMPLE_JSON), Dataset.class));
    assertThat(MAPPER.writeValueAsString(DATASET)).isEqualTo(expected);
  }

  @Test
  public void deserializesFromJSON() throws Exception {
    assertThat(MAPPER.readValue(fixture(DATASET_SAMPLE_JSON), Dataset.class)).isEqualTo(DATASET);
  }
}
