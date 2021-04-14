package marquez;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@org.junit.jupiter.api.Tag("IntegrationTests")
public class OpenLineageIntegrationTest extends BaseIntegrationTest {
  public static String EVENT_REQUIRED = "open_lineage/event_required_only.json";
  public static String EVENT_SIMPLE = "open_lineage/event_simple.json";
  public static String EVENT_FULL = "open_lineage/event_full.json";
  public static String EVENT_UNICODE = "open_lineage/event_unicode.json";
  public static String EVENT_LARGE = "open_lineage/event_large.json";
  public static String NULL_NOMINAL_END_TIME = "open_lineage/null_nominal_end_time.json";

  public static List<String> data() {
    return Arrays.asList(
        EVENT_FULL,
        EVENT_SIMPLE,
        EVENT_REQUIRED,
        EVENT_UNICODE,
        EVENT_LARGE,
        NULL_NOMINAL_END_TIME);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testOpenLineage(String input) throws IOException {
    URL resource = Resources.getResource(input);
    String body = Resources.toString(resource, Charset.defaultCharset());

    CompletableFuture<Integer> resp =
        this.sendLineage(body)
            .thenApply(HttpResponse::statusCode)
            .whenComplete(
                (val, error) -> {
                  if (error != null) {
                    Assertions.fail("Could not complete request");
                  }
                });

    Assertions.assertEquals((Integer) 201, resp.join());
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testSerialization(String input) throws IOException {
    testSerialization(Utils.newObjectMapper(), input);
  }

  // Test object mapper with listed jackson requirements
  @ParameterizedTest
  @MethodSource("data")
  public void testRequiredObjectMapper(String input) throws IOException {
    testSerialization(getMapper(), input);
  }

  public ObjectMapper getMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

    return mapper;
  }

  public void testSerialization(ObjectMapper mapper, String input) throws IOException {
    URL in = Resources.getResource(input);

    LineageEvent lineageEvent = mapper.readValue(in, LineageEvent.class);
    String out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(lineageEvent);

    Assertions.assertEquals(mapper.readTree(in), mapper.readTree(out));
  }
}
