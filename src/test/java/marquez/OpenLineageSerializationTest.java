package marquez;

import static marquez.OpenLineageIntegrationTest.EVENT_FULL;
import static marquez.OpenLineageIntegrationTest.EVENT_REQUIRED;
import static marquez.OpenLineageIntegrationTest.EVENT_SIMPLE;
import static marquez.OpenLineageIntegrationTest.EVENT_UNICODE;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dropwizard.util.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import marquez.service.models.LineageEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OpenLineageSerializationTest {
  @Parameters(name = "lineage_{0}_serialization")
  public static List<String> data() {
    return Arrays.asList(EVENT_FULL, EVENT_SIMPLE, EVENT_REQUIRED, EVENT_UNICODE);
  }

  @Parameter public String input;

  @Test
  public void test_serialization() throws IOException {
    URL in = Resources.getResource(input);

    ObjectMapper mapper = getMapper();

    LineageEvent lineageEvent = mapper.readValue(in, LineageEvent.class);
    String out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(lineageEvent);

    assertEquals(mapper.readTree(in), mapper.readTree(out));
  }

  public ObjectMapper getMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

    return mapper;
  }
}
