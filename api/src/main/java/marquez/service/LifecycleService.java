package marquez.service;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.LifecycleDao;

/** ... */
@Slf4j
public class LifecycleService {
  private final LifecycleListener lifecycleListener;
  private final LifecycleDao lifecycleDao;

  public LifecycleService(@NonNull final LifecycleDao lifecycleDao) {
    this(
        new LifecycleListener() {
          @Override
          public void onLifecycleChange(@NonNull Lifecycle.Event event) {
            // noop
            // 1) Add to kafka topic
            // 2) Send notification ...
            // 3)
          }
        },
        lifecycleDao);
  }

  public LifecycleService(
      @NonNull final LifecycleListener lifecycleListener,
      @NonNull final LifecycleDao lifecycleDao) {
    this.lifecycleListener = lifecycleListener;
    this.lifecycleDao = lifecycleDao;
  }

  /** ... */
  public void handleLifecycleEvent(@NonNull Lifecycle.Event lifecycleEvent) {
    log.info("Received lifecycle event: {}", lifecycleEvent);
    lifecycleDao.insertLifecycleEvent(UUID.randomUUID(), lifecycleEvent);
    // ...
    lifecycleListener.onLifecycleChange(lifecycleEvent);
  }

  public interface Lifecycle {
    /** ... */
    @Data
    class Event {
      @NonNull State state;
      @NonNull String namespace;
      @NonNull Instant transitionedAt;
      @NonNull String message;
    }
    /** ... */
    enum State {
      ADDED,
      UPDATED,
      DELETED,
      NEW_VERSION;
    }
  }
}
