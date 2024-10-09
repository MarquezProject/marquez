/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import io.openlineage.server.OpenLineage;
import lombok.NonNull;

/** Enum of supported run states. */
public enum RunState {
  NEW,
  RUNNING,
  COMPLETED,
  ABORTED,
  FAILED,
  OTHER;

  /* Returns true if this state is running. */
  public boolean isStarting() {
    return this == RUNNING;
  }

  /* Returns true if this state is complete. */
  public boolean isComplete() {
    return this == COMPLETED;
  }

  /* Returns true if this state is done. */
  public boolean isDone() {
    return this == COMPLETED || this == ABORTED || this == FAILED;
  }

  /* ... */
  public static RunState forType(@NonNull OpenLineage.RunEvent.EventType type) {
    switch (type) {
      case START:
      case RUNNING:
        return RUNNING;
      case COMPLETE:
        return COMPLETED;
      case ABORT:
        return ABORTED;
      case FAIL:
        return FAILED;
      case OTHER:
        return OTHER;
    }
    throw new IllegalArgumentException("Unknown 'OpenLineage.RunEvent.EventType': " + type);
  }
}
