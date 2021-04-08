package marquez;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@org.junit.jupiter.api.Tag("IntegrationTests")
public class SparkAgentIntegrationTest extends BaseIntegrationTest {

  public static List<Path> data() throws IOException {
    String prefix = "../integrations/spark/integrations";
    List<Path> paths = Files.list(Paths.get(prefix + "/sparkrdd")).collect(Collectors.toList());
    List<Path> sql = Files.list(Paths.get(prefix + "/sparksql")).collect(Collectors.toList());
    paths.addAll(sql);
    return paths;
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testOpenLineage(Path input) throws IOException {
    String body = new String(Files.readAllBytes(input));

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
}
