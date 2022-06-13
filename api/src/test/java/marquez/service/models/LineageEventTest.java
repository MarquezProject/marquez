/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.dropwizard.util.Resources;
import io.openlineage.client.OpenLineage.RunEvent;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Arrays;
import java.util.List;
import marquez.common.Utils;
import marquez.common.models.FlexibleDateTimeDeserializer;
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
  public static String EVENT_NANOSECOND_TIME = "open_lineage/event_required_nanoseconds.json";
  public static String EVENT_TIME_WITH_NO_TIMEZONE = "open_lineage/event_required_no_timezone.json";

  public static List<String> data() {
    return Arrays.asList(
        EVENT_FULL,
        EVENT_SIMPLE,
        EVENT_REQUIRED,
        EVENT_UNICODE,
        EVENT_LARGE,
        NULL_NOMINAL_END_TIME,
        EVENT_NAMESPACE_NAMING,
        EVENT_NANOSECOND_TIME,
        EVENT_TIME_WITH_NO_TIMEZONE);
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
    assertThat(converted)
        .usingRecursiveComparison()
        .withEqualsForFields(
            (ZonedDateTime a, ZonedDateTime b) -> a.toInstant().equals(b.toInstant()), "eventTime")
        .isEqualTo(expectedEvent);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testSerialization(String input) throws IOException {
    testSerialization(Utils.newObjectMapper(), input);
  }

  public void testSerialization(ObjectMapper mapper, String expectedFile) throws IOException {
    URL expectedResource = Resources.getResource(expectedFile);
    LineageEvent deserialized = mapper.readValue(expectedResource, LineageEvent.class);
    String serialized = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deserialized);

    JsonNode expectedNode = mapper.readTree(expectedResource);
    TemporalAccessor parsedEventTime =
        FlexibleDateTimeDeserializer.DATE_TIME_OPTIONAL_OFFSET.parse(
            expectedNode.get("eventTime").textValue());
    ZonedDateTime zonedDateTime =
        parsedEventTime.query(TemporalQueries.zone()) != null
            ? ZonedDateTime.from(parsedEventTime)
            : LocalDateTime.from(parsedEventTime).atZone(ZoneId.systemDefault());
    ((ObjectNode) expectedNode)
        .set(
            "eventTime",
            new TextNode(
                FlexibleDateTimeDeserializer.DATE_TIME_OPTIONAL_OFFSET.format(zonedDateTime)));
    JsonNode actualNode = mapper.readTree(serialized);
    assertThat(actualNode).isEqualTo(expectedNode);
  }
}
