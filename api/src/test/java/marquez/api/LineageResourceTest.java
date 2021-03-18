package marquez.api;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import marquez.BaseIntegrationTest;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.Job;
import marquez.service.models.LineageEvent.Run;
import org.junit.Test;

public class LineageResourceTest extends BaseIntegrationTest {

  @Test
  public void testLineageEndpoint() {
    LineageEvent pickApples =
        LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(now().minusMinutes(3))
            .job(Job.builder().name("pick_apples").namespace("lineage").build())
            .run(new Run(UUID.randomUUID().toString(), null))
            .inputs(ImmutableList.of(new Dataset("lineage", "red_delicious", null)))
            .outputs(ImmutableList.of(new Dataset("lineage", "apples", null)))
            .producer("producer")
            .build();
    HttpResponse<String> resp = sendLineage(pickApples);
    assertEquals(resp.statusCode(), 201);

    LineageEvent makePie =
        LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(now().minusMinutes(2))
            .job(Job.builder().name("make_pie").namespace("lineage").build())
            .run(new Run(UUID.randomUUID().toString(), null))
            .inputs(ImmutableList.of(new Dataset("lineage", "apples", null)))
            .outputs(ImmutableList.of(new Dataset("lineage", "apple_pie", null)))
            .producer("producer")
            .build();
    resp = sendLineage(makePie);
    assertEquals(resp.statusCode(), 201);

    Response response = getLineage("lineage", "pick_apples", 10, expect200);
    GraphqlResponse<LineageResponse> lineage =
        response.readEntity(new GenericType<GraphqlResponse<LineageResponse>>() {});
    assertEquals(lineage.data.lineageFromJob.graph.size(), 5);
  }

  private ZonedDateTime now() {
    return Instant.now().atZone(ZoneId.systemDefault());
  }

  public static class LineageResponse {
    public LineageGraph lineageFromJob;
  }

  public static class LineageGraph {
    public List<Map> graph;
  }
}
