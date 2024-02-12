package marquez.db.v2;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import io.dropwizard.lifecycle.Managed;
import io.openlineage.server.OpenLineage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.async.JdbiExecutor;

/** ... */
public class MetadataDb {
  private final ConcurrentLinkedQueue<BatchSqlWriteCall> nonBlockingDbCallQueue;
  private final JdbiExecutor nonBlockingDbCallExecutor;

  /* ... */
  private MetadataDb(@NonNull final ManagedConnectionPool connectionPool) {
    this.nonBlockingDbCallQueue = new ConcurrentLinkedQueue<>();
    this.nonBlockingDbCallExecutor =
        JdbiExecutor.create(
            Jdbi.create(connectionPool),
            Executors.newFixedThreadPool(connectionPool.getMaximumPoolSize()));
    ;
    // ...
    new MetadataDb.BatchSqlWriter(nonBlockingDbCallQueue, nonBlockingDbCallExecutor).start();
  }

  /* ... */
  public static MetadataDb newInstance(@NonNull final ManagedConnectionPool connectionPool) {
    return new MetadataDb(connectionPool);
  }

  /* ... */
  public void write(@NotNull OpenLineage.RunEvent olRunEvent) {
    nonBlockingDbCallQueue.offer(BatchSqlWriteCall.newCallFor(olRunEvent));
  }

  /* ... */
  static final class BatchSqlWriter implements Managed {
    private final ConcurrentLinkedQueue<BatchSqlWriteCall> nonBlockingDbCallQueue;
    private final JdbiExecutor nonBlockingDbCall;

    private final ScheduledExecutorService pollDbCallQueueScheduler;
    private final int initialPollDelayMs;
    private final int pollPeriodMs;
    private final AtomicBoolean isPolling;

    /* ... */
    public BatchSqlWriter(
        @NonNull final ConcurrentLinkedQueue<BatchSqlWriteCall> nonBlockingDbCallQueue,
        @NonNull final JdbiExecutor nonBlockingDbCall) {
      this.nonBlockingDbCallQueue = nonBlockingDbCallQueue;
      this.nonBlockingDbCall = nonBlockingDbCall;
      this.pollDbCallQueueScheduler = Executors.newSingleThreadScheduledExecutor();
      this.initialPollDelayMs = 1000;
      this.pollPeriodMs = 2000;
      this.isPolling = new AtomicBoolean(false);
    }

    @Override
    public void start() {
      pollDbCallQueueScheduler.scheduleAtFixedRate(
          () -> {
            // ...
            if (isPolling.get()) {
              return;
            }
            // ...
            isPolling.set(true);
            try {
              while (true) {
                final BatchSqlWriteCall batchSqlWriteCall = nonBlockingDbCallQueue.poll();
                if (batchSqlWriteCall == null) {
                  break;
                }
                nonBlockingDbCall.useHandle(batchSqlWriteCall);
              }
            } finally {
              isPolling.set(false);
            }
          },
          initialPollDelayMs,
          pollPeriodMs,
          MILLISECONDS);
    }

    @Override
    public void stop() {
      pollDbCallQueueScheduler.shutdown();
    }
  }
}
