package marquez.spark.agent;

import static org.mockito.Mockito.mock;

import io.openlineage.spark.agent.OpenLineageContext;
import io.openlineage.spark.agent.SparkAgent;
import marquez.spark.agent.lifecycle.StaticExecutionContextFactory;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

/**
 * JUnit extension that invokes the {@link SparkAgent} by installing the {@link ByteBuddyAgent} to
 * instrument classes. This will allow the {@link java.lang.instrument.ClassFileTransformer}s in the
 * {@link io.openlineage.spark.agent.transformers} package to transform the byte code of target
 * classes as they're loaded.
 *
 * <p>Note that this extension has to be annotated on any class that interacts with any of the
 * transformed classes (i.e., {@link org.apache.spark.SparkContext}, {@link
 * org.apache.spark.sql.SparkSession}, etc.). Once a class has been loaded, it won't go through the
 * {@link java.lang.instrument.ClassFileTransformer} process again. If a test doesn't use this
 * extension and ends up running before other Spark tests, those subsequent tests will fail.
 */
public class SparkAgentTestExtension implements BeforeAllCallback, BeforeEachCallback {
  public static final OpenLineageContext marquezContext = mock(OpenLineageContext.class);

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    ByteBuddyAgent.install();
    SparkAgent.premain(
        "/api/v1/namespaces/ns_name/jobs/job_name/runs/ea445b5c-22eb-457a-8007-01c7c52b6e54",
        ByteBuddyAgent.getInstrumentation(),
        new StaticExecutionContextFactory(marquezContext));
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    Mockito.reset(marquezContext);
  }
}
