package marquez.spark.agent;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import marquez.client.Backends;
import marquez.spark.agent.transformers.ActiveJobTransformer;
import marquez.spark.agent.transformers.PairRDDFunctionsTransformer;
import marquez.spark.agent.transformers.SparkContextTransformer;

public class MarquezAgent {
  private static final Logger logger = LoggerFactory.getLogger(MarquezAgent.class);

  public static void instrument(String agentArgument, Instrumentation instrumentation) throws Exception {
    instrumentation.addTransformer(new ActiveJobTransformer());
    instrumentation.addTransformer(new SparkContextTransformer());
    instrumentation.addTransformer(new PairRDDFunctionsTransformer());
  }

  public static void premain(String agentArgument, Instrumentation instrumentation) throws Exception {
    logger.info("MarquezAgent.premain " + agentArgument);
    SparkListener.init(agentArgument, Backends.newLoggingBackend());
    instrument(agentArgument, instrumentation);
    addShutDownHook();
  }

  public static void main(String agentArgument, Instrumentation instrumentation) throws Exception {
    logger.info("MarquezAgent.main " + agentArgument);
    SparkListener.init(agentArgument, Backends.newLoggingBackend());
    instrument(agentArgument, instrumentation);
    addShutDownHook();
  }


  private static void addShutDownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
      @Override
      public void run() {
        SparkListener.close();
      }
    }));
  }
}
