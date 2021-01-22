package marquez.spark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.Resources;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import marquez.spark.agent.MarquezAgent;
import marquez.spark.agent.MarquezContext;
import marquez.spark.agent.client.LineageEvent;
import marquez.spark.agent.client.OpenLineageClient;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import scala.Tuple2;

public class LibraryTest {
  static MarquezContext marquezContext;

  @BeforeClass
  public static void setUp() throws Exception {
    marquezContext = mock(MarquezContext.class);
    ByteBuddyAgent.install();
    MarquezAgent.premain("", ByteBuddyAgent.getInstrumentation(), marquezContext);
  }

  @Test
  public void testSparkSql() throws JsonProcessingException {
    reset(marquezContext);
    when(marquezContext.getJobNamespace()).thenReturn("ns_name");
    when(marquezContext.getJobName()).thenReturn("job_name");
    when(marquezContext.getParentRunId()).thenReturn("ea445b5c-22eb-457a-8007-01c7c52b6e54");

    final SparkSession spark =
        SparkSession.builder()
            .master("local[*]")
            .appName("Word Count")
            .config("spark.driver.host", "127.0.0.1")
            .config("spark.driver.bindAddress", "127.0.0.1")
            .getOrCreate();

    URL url = Resources.getResource("data.txt");
    final Dataset<String> data = spark.read().textFile(url.getPath()).cache();

    final long numAs = data.filter(s -> s.contains("a")).count();
    final long numBs = data.filter(s -> s.contains("b")).count();

    System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
    spark.stop();

    ArgumentCaptor<LineageEvent> lineageEvent = ArgumentCaptor.forClass(LineageEvent.class);
    Mockito.verify(marquezContext, times(4)).emit(lineageEvent.capture());
    List<LineageEvent> events = lineageEvent.getAllValues();
    assertEquals(4, events.size());
    verifyEvents(events);
  }

  @Test
  public void testRdd() throws JsonProcessingException {
    reset(marquezContext);
    when(marquezContext.getJobNamespace()).thenReturn("ns_name");
    when(marquezContext.getJobName()).thenReturn("job_name");
    when(marquezContext.getParentRunId()).thenReturn("ea445b5c-22eb-457a-8007-01c7c52b6e54");

    URL url = Resources.getResource("data.txt");
    SparkConf conf = new SparkConf().setAppName("Word Count").setMaster("local[*]");
    JavaSparkContext sc = new JavaSparkContext(conf);
    JavaRDD<String> textFile = sc.textFile(url.getPath());

    textFile
        .flatMap(s -> Arrays.asList(s.split(" ")).iterator())
        .mapToPair(word -> new Tuple2<>(word, 1))
        .reduceByKey((a, b) -> a + b)
        .count();

    sc.stop();

    ArgumentCaptor<LineageEvent> lineageEvent = ArgumentCaptor.forClass(LineageEvent.class);
    Mockito.verify(marquezContext, times(2)).emit(lineageEvent.capture());
    List<LineageEvent> events = lineageEvent.getAllValues();
    assertEquals(2, events.size());
    verifyEvents(events);
  }

  private void verifyEvents(List<LineageEvent> events) throws JsonProcessingException {
    for (LineageEvent event : events) {
      assertNotNull(event.getEventTime());
      assertNotNull(event.getEventType());
      assertNotNull(event.getJob());
      assertNotNull(event.getJob().getName());
      assertNotNull(event.getJob().getNamespace());
      assertEquals("job_name", event.getJob().getName());
      assertEquals("ns_name", event.getJob().getNamespace());
      assertNotNull(event.getRun());
      assertNotNull(event.getRun().getRunId());
      assertEquals("ea445b5c-22eb-457a-8007-01c7c52b6e54", event.getRun().getRunId());
      assertNotNull(event.getProducer());

      assertNotNull(event.getInputs());
      assertEquals(1, event.getInputs().size());
      assertNotNull(event.getInputs().get(0).getName());
      assertTrue(event.getInputs().get(0).getName().endsWith("data.txt"));
      assertEquals("ns_name", event.getInputs().get(0).getNamespace());

      assertNotNull(event.getOutputs());
      assertEquals(0, event.getOutputs().size());

      assertNotNull(
          "Event can serialize", OpenLineageClient.getObjectMapper().writeValueAsString(event));
    }
  }
}
