package marquez.spark.agent.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.Resources;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.apache.spark.sql.SparkSession$;
import org.junit.After;
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
    MarquezAgent.premain(
        "/api/v1/namespaces/ns_name/jobs/job_name/runs/ea445b5c-22eb-457a-8007-01c7c52b6e54",
        ByteBuddyAgent.getInstrumentation(),
        new StaticExecutionContextFactory(marquezContext));
  }

  @After
  public void tearDown() throws Exception {
    SparkSession$.MODULE$.cleanupAnyExistingSession();
  }

  @Test
  public void testSparkSql() throws IOException {
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
    final Dataset<String> data = spark.read().textFile(url.getPath());

    final long numAs = data.filter(s -> s.contains("a")).count();
    final long numBs = data.filter(s -> s.contains("b")).count();

    System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
    spark.stop();

    ArgumentCaptor<LineageEvent> lineageEvent = ArgumentCaptor.forClass(LineageEvent.class);
    Mockito.verify(marquezContext, times(4)).emit(lineageEvent.capture());
    List<LineageEvent> events = lineageEvent.getAllValues();

    updateSnapshots("sparksql", events);

    assertEquals(4, events.size());

    for (int i = 0; i < events.size(); i++) {
      LineageEvent event = events.get(i);
      String snapshot =
          new String(
              Files.readAllBytes(
                  Paths.get(String.format("integrations/%s/%d.json", "sparksql", i + 1))));
      assertEquals(
          snapshot,
          OpenLineageClient.getObjectMapper()
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(event));
    }
    verifySerialization(events);
  }

  @Test
  public void testRdd() throws IOException {
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
        .reduceByKey(Integer::sum)
        .count();

    sc.stop();

    ArgumentCaptor<LineageEvent> lineageEvent = ArgumentCaptor.forClass(LineageEvent.class);
    Mockito.verify(marquezContext, times(2)).emit(lineageEvent.capture());
    List<LineageEvent> events = lineageEvent.getAllValues();
    assertEquals(2, events.size());

    updateSnapshots("sparkrdd", events);

    for (int i = 0; i < events.size(); i++) {
      LineageEvent event = events.get(i);
      String snapshot =
          new String(
              Files.readAllBytes(
                  Paths.get(String.format("integrations/%s/%d.json", "sparkrdd", i + 1))));
      assertEquals(
          snapshot,
          OpenLineageClient.getObjectMapper()
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(event));
    }

    verifySerialization(events);
  }

  private void verifySerialization(List<LineageEvent> events) throws JsonProcessingException {
    for (LineageEvent event : events) {
      assertNotNull(
          "Event can serialize", OpenLineageClient.getObjectMapper().writeValueAsString(event));
    }
  }

  private void updateSnapshots(String prefix, List<LineageEvent> events) {
    if (System.getenv().containsKey("UPDATE_SNAPSHOT")) {
      for (int i = 0; i < events.size(); i++) {
        LineageEvent event = events.get(i);
        try {
          String url = String.format("integrations/%s/%d.json", prefix, i + 1);
          FileWriter myWriter = new FileWriter(url);
          myWriter.write(
              OpenLineageClient.getObjectMapper()
                  .writerWithDefaultPrettyPrinter()
                  .writeValueAsString(event));
          myWriter.close();
        } catch (IOException e) {
          e.printStackTrace();
          fail();
        }
      }
    }
  }
}
