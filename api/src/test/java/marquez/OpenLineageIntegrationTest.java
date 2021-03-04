package marquez;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dropwizard.util.Resources;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import marquez.common.Utils;
import marquez.service.models.LineageEvent;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
@Category(IntegrationTests.class)
public class OpenLineageIntegrationTest extends BaseIntegrationTest {
  public static String EVENT_REQUIRED = "open_lineage/event_required_only.json";
  public static String EVENT_SIMPLE = "open_lineage/event_simple.json";
  public static String EVENT_FULL = "open_lineage/event_full.json";
  public static String EVENT_UNICODE = "open_lineage/event_unicode.json";
  public static String EVENT_LARGE = "open_lineage/event_large.json";

  @Parameters(name = "{0}")
  public static List<String> data() {
    return Arrays.asList(EVENT_FULL, EVENT_SIMPLE, EVENT_REQUIRED, EVENT_UNICODE, EVENT_LARGE);
  }

  @Parameter public String input;

  @Test
  public void testOpenLineage() throws IOException {
    URL resource = Resources.getResource(input);
    String body = Resources.toString(resource, Charset.defaultCharset());

    CompletableFuture<Integer> resp =
        this.sendLineage(body)
            .thenApply(HttpResponse::statusCode)
            .whenComplete(
                (val, error) -> {
                  if (error != null) {
                    fail("Could not complete request");
                  }
                });

    assertEquals((Integer) 201, resp.join());
  }

  @Test
  public void testSerialization() throws IOException {
    testSerialization(Utils.newObjectMapper());
  }

  // Test object mapper with listed jackson requirements
  @Test
  public void testRequiredObjectMapper() throws IOException {
    testSerialization(getMapper());
  }

  public ObjectMapper getMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

    return mapper;
  }

  public void testSerialization(ObjectMapper mapper) throws IOException {
    URL in = Resources.getResource(input);

    LineageEvent lineageEvent = mapper.readValue(in, LineageEvent.class);
    String out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(lineageEvent);

    assertEquals(mapper.readTree(in), mapper.readTree(out));
  }
}
