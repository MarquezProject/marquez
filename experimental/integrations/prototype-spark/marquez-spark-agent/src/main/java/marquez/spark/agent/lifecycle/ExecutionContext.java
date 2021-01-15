package marquez.spark.agent.lifecycle;

import org.apache.spark.scheduler.ActiveJob;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;

public interface ExecutionContext {

  abstract void setActiveJob(ActiveJob activeJob);

  abstract void start(SparkListenerJobStart jobStart);

  abstract void end(SparkListenerJobEnd jobEnd);
}
