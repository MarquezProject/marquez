package marquez.spark.agent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkContext;
import org.apache.spark.rdd.PairRDDFunctions;
import org.apache.spark.rdd.RDD;
import org.apache.spark.scheduler.ActiveJob;
import org.apache.spark.scheduler.SparkListenerInterface;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.sql.execution.ui.SparkListenerSQLExecutionEnd;
import org.apache.spark.sql.execution.ui.SparkListenerSQLExecutionStart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import marquez.client.Backend;
import marquez.spark.agent.lifecycle.ExecutionContext;
import marquez.spark.agent.lifecycle.RddExecutionContext;
import marquez.spark.agent.lifecycle.SparkSQLExecutionContext;
import marquez.spark.agent.transformers.ActiveJobTransformer;
import marquez.spark.agent.transformers.PairRDDFunctionsTransformer;


public class SparkListener {
  private static final Logger logger = LoggerFactory.getLogger(SparkListener.class);

  private static final Map<Long, SparkSQLExecutionContext> sparkSqlExecutionRegistry = Collections.synchronizedMap(new HashMap<>());

  public static SparkSQLExecutionContext getSparkSQLExecutionContext(long executionId) {
    SparkSQLExecutionContext sparkSQLExecutionContext = sparkSqlExecutionRegistry.get(executionId);
    if (sparkSQLExecutionContext == null) {
      sparkSQLExecutionContext = new SparkSQLExecutionContext(executionId, marquezContext);
      sparkSqlExecutionRegistry.put(executionId, sparkSQLExecutionContext);
    }
    return sparkSQLExecutionContext;
  }

  private static final Map<Integer, ExecutionContext> rddExecutionRegistry = Collections.synchronizedMap(new HashMap<>());

  public static ExecutionContext getExecutionContext(int jobId) {
    ExecutionContext rddExecutionContext = rddExecutionRegistry.get(jobId);
    if (rddExecutionContext == null) {
      rddExecutionContext = new RddExecutionContext(jobId, marquezContext);
      rddExecutionRegistry.put(jobId, rddExecutionContext);
    }
    return rddExecutionContext;
  }

  public static ExecutionContext getExecutionContext(int jobId, long executionId) {
    ExecutionContext executionContext = getSparkSQLExecutionContext(executionId);
    rddExecutionRegistry.put(jobId, executionContext);
    return executionContext;
  }

  private static Map<RDD<?>, Configuration> outputs = Collections.synchronizedMap(new HashMap<>());

  public static Configuration removeConfigForRDD(RDD<?> rdd) {
    return outputs.remove(rdd);
  }

  private static MarquezContext marquezContext;

  /**
   * called by the agent on init with the provided argument
   * @param agentArgument
   */
  public static void init(String agentArgument, Backend backend) {
    logger.info("init: " + agentArgument);
    marquezContext = new MarquezContext(agentArgument, backend);
    clear();
  }

  private static void clear() {
    sparkSqlExecutionRegistry.clear();
    rddExecutionRegistry.clear();
    outputs.clear();
  }

  /**
   * Called through the agent to register every new job and get access to the RDDs
   * @see ActiveJobTransformer
   * @param activeJob
   */
  public static void registerActiveJob(ActiveJob activeJob) {
    logger.info("registerActiveJob: " + activeJob);
    String executionIdProp = activeJob.properties().getProperty("spark.sql.execution.id");
    ExecutionContext context;
    if (executionIdProp != null) {
      long executionId = Long.parseLong(executionIdProp);
      context = getExecutionContext(activeJob.jobId(), executionId);
    } else {
      context = getExecutionContext(activeJob.jobId());
    }
    context.setActiveJob(activeJob);
  }

  /**
   * called through the agent when writing with the RDD API as the RDDs do not contain the output information
   * @see PairRDDFunctionsTransformer
   * @param pairRDDFunctions the wrapping RDD containing the rdd to save
   * @param conf the write config
   */
  public static void registerOutput(PairRDDFunctions<?, ?> pairRDDFunctions, Configuration conf) {
    logger.info("registerOutput: " + pairRDDFunctions + " " + conf);
    Field[] declaredFields = pairRDDFunctions.getClass().getDeclaredFields();
    for (Field field : declaredFields) {
      if (field.getName().endsWith("self") && RDD.class.isAssignableFrom(field.getType())) {
        field.setAccessible(true);
        try {
          RDD<?> rdd = (RDD<?>)field.get(pairRDDFunctions);
          outputs.put(rdd, conf);
        } catch (IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace(System.out);
        }
      }
    }
  }

  /**
   * called by the SparkListener when a spark-sql (Dataset api) execution starts
   * @param startEvent
   */
  private static void sparkSQLExecStart(SparkListenerSQLExecutionStart startEvent) {
    logger.info("sparkSQLExecStart: " + startEvent);
    SparkSQLExecutionContext context = getSparkSQLExecutionContext(startEvent.executionId());
    context.start(startEvent);
  }

  /**
   * called by the SparkListener when a spark-sql (Dataset api) execution ends
   * @param endEvent
   */
  private static void sparkSQLExecEnd(SparkListenerSQLExecutionEnd endEvent) {
    logger.info("sparkSQLExecEnd: " + endEvent);
    SparkSQLExecutionContext context = sparkSqlExecutionRegistry.remove(endEvent.executionId());
    context.end(endEvent);
  }

  /**
   * called by the SparkListener when a job starts
   * @param jobStart
   */
  private static void jobStarted(SparkListenerJobStart jobStart) {
    logger.info("jobStarted: " + jobStart);
    ExecutionContext context = getExecutionContext(jobStart.jobId());
    context.start(jobStart);
  }

  /**
   * called by the SparkListener when a job ends
   * @param jobStart
   */
  private static void jobEnded(SparkListenerJobEnd jobEnd) {
    logger.info("jobEnded: " + jobEnd);
    ExecutionContext context = rddExecutionRegistry.remove(jobEnd.jobId());
    context.end(jobEnd);
  }

  /**
   * called through the agent when creating the Spark context
   * We register a new SparkListener
   * @param context the spark context
   */
  public static void instrument(SparkContext context) {
    logger.info("instrument: " + context);
    Class<?>[] interfaces = {SparkListenerInterface.class};
    SparkListenerInterface listener = (SparkListenerInterface)Proxy.newProxyInstance(SparkListener.class.getClassLoader(), interfaces, new InvocationHandler(){

      int counter = 0;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("onExecutorMetricsUpdate")
            || method.getName().equals("onBlockUpdated")
            || method.getName().equals("onTaskStart")
            || method.getName().equals("onTaskEnd")
            || method.getName().equals("onStageSubmitted")
            || method.getName().equals("onStageCompleted")
            ) {
          return null;
        }
        int call = counter ++;
        String prefix = "MQZ - " + call;
        if (args.length == 1 & args[0]!=null) {
          Object arg = args[0];
          String eventType = "(" + arg.getClass().getSimpleName() + ")";
          logger.info(  prefix + " -: " + method.getName() + eventType);
          if (method.getName().equals("onJobStart") && arg instanceof SparkListenerJobStart) {
            jobStarted((SparkListenerJobStart)args[0]);
          } else if (method.getName().equals("onJobEnd") && arg instanceof SparkListenerJobEnd) {
            jobEnded((SparkListenerJobEnd)arg);
          } else if (method.getName().equals("onOtherEvent") && arg instanceof SparkListenerSQLExecutionStart) {
            sparkSQLExecStart((SparkListenerSQLExecutionStart)arg);
          } else if (method.getName().equals("onOtherEvent") && arg instanceof SparkListenerSQLExecutionEnd) {
            sparkSQLExecEnd((SparkListenerSQLExecutionEnd)arg);
          } else {
            logger.info(  prefix + " UNEXPECTED -(event)----" + arg);
          }
        } else {
          logger.info(  prefix + " UNEXPECTED -(method)----" + method);
          for (Object arg : args) {
            logger.info(prefix + " UNEXPECTED -(arg)-------  " + arg);
          }
        }
        return null;
      }
    });
    context.addSparkListener(listener);
  }

  /**
   * To close the underlying resources.
   */
  public static void close() {
    clear();
    marquezContext.close();
  }
}
