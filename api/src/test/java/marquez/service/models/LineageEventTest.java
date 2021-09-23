package marquez.service.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dropwizard.util.Resources;
import io.openlineage.client.OpenLineage.RunEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import marquez.common.Utils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class LineageEventTest {
  public static String EVENT_REQUIRED = "open_lineage/event_required_only.json";
  public static String EVENT_SIMPLE = "open_lineage/event_simple.json";
  public static String EVENT_FULL = "open_lineage/event_full.json";
  public static String EVENT_UNICODE = "open_lineage/event_unicode.json";
  public static String EVENT_LARGE = "open_lineage/event_large.json";
  public static String NULL_NOMINAL_END_TIME = "open_lineage/null_nominal_end_time.json";
  public static String EVENT_NAMESPACE_NAMING = "open_lineage/event_namespace_naming.json";

  public static List<String> data() {
    return Arrays.asList(
        EVENT_FULL,
        EVENT_SIMPLE,
        EVENT_REQUIRED,
        EVENT_UNICODE,
        EVENT_LARGE,
        NULL_NOMINAL_END_TIME,
        EVENT_NAMESPACE_NAMING);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testIsomorphicOpenLineageEvents(String inputFile) throws IOException {
    URL expectedResource = Resources.getResource(inputFile);
    ObjectMapper objectMapper = Utils.newObjectMapper();
    RunEvent expectedEvent = objectMapper.readValue(expectedResource, RunEvent.class);
    LineageEvent lineageEvent = objectMapper.readValue(expectedResource, LineageEvent.class);
    RunEvent converted =
        objectMapper.readValue(objectMapper.writeValueAsString(lineageEvent), RunEvent.class);
    assertThat(converted).usingRecursiveComparison().isEqualTo(expectedEvent);
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

  public void testSerialization(ObjectMapper mapper, String expectedFile) throws IOException {
    URL expectedResource = Resources.getResource(expectedFile);
    LineageEvent expected = mapper.readValue(expectedResource, LineageEvent.class);
    String serialized = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected);

    assertThat(mapper.readTree(serialized)).isEqualTo(mapper.readTree(expectedResource));
  }
}
