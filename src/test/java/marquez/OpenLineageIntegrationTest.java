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
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
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

  @Parameters(name = "{0}")
  public static List<String> data() {
    return Arrays.asList(EVENT_FULL, EVENT_SIMPLE, EVENT_REQUIRED, EVENT_UNICODE);
  }

  @Parameter public String input;

  @Test
  public void testApp_openLineage() throws IOException {
    URL resource = Resources.getResource(input);
    String lineageArr = Resources.toString(resource, Charset.defaultCharset());
    HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).build();

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/v1/lineage"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(lineageArr))
            .build();

    CompletableFuture<Integer> resp =
        client
            .sendAsync(request, BodyHandlers.ofString())
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
  public void test_serialization() throws IOException {
    URL in = Resources.getResource(input);

    ObjectMapper mapper = Utils.newObjectMapper();

    LineageEvent lineageEvent = mapper.readValue(in, LineageEvent.class);
    String out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(lineageEvent);

    assertEquals(mapper.readTree(in), mapper.readTree(out));
  }
}
