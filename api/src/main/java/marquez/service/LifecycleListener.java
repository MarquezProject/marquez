package marquez.service;

import marquez.service.LifecycleService.Lifecycle;

/** ... */
public interface LifecycleListener {
  /** ... */
  void onLifecycleChange(Lifecycle.Event event);
}
