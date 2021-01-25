package marquez;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
@Category(IntegrationTests.class)
public class SparkAgentIntegrationTest extends BaseIntegrationTest {
  @Parameters(name = "{0}")
  public static List<Path> data() throws IOException {
    String prefix = "experimental/integrations/marquez-spark-agent/integrations";
    List<Path> paths = Files.list(Paths.get(prefix + "/sparkrdd")).collect(Collectors.toList());
    List<Path> sql = Files.list(Paths.get(prefix + "/sparksql")).collect(Collectors.toList());
    paths.addAll(sql);
    return paths;
  }

  @Parameter public Path input;

  @Test
  public void testOpenLineage() throws IOException {
    String body = new String(Files.readAllBytes(input));

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
}
