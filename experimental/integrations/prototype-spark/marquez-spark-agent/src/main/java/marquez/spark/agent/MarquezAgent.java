package marquez.spark.agent;

import java.lang.instrument.Instrumentation;

import marquez.client.Backends;
import marquez.spark.agent.transformers.ActiveJobTransformer;
import marquez.spark.agent.transformers.PairRDDFunctionsTransformer;
import marquez.spark.agent.transformers.SparkContextTransformer;

public class MarquezAgent {

  public static void instrument(String agentArgument, Instrumentation instrumentation) throws Exception {
    instrumentation.addTransformer(new ActiveJobTransformer());
    instrumentation.addTransformer(new SparkContextTransformer());
    instrumentation.addTransformer(new PairRDDFunctionsTransformer());
  }

  public static void premain(String agentArgument, Instrumentation instrumentation) throws Exception {
    System.out.println("MarquezAgent.premain");
    SparkListener.init(agentArgument, Backends.newLoggingBackend());
    instrument(agentArgument, instrumentation);
    addShutDownHook();
  }

  public static void main(String agentArgument, Instrumentation instrumentation) throws Exception {
    System.out.println("MarquezAgent.main");
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
