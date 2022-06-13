/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

public enum RunState {
  NEW,
  RUNNING,
  COMPLETED,
  ABORTED,
  FAILED;

  /* Returns true if this state is complete. */
  public boolean isComplete() {
    return this == COMPLETED;
  }

  /* Returns true if this state is running. */
  public boolean isStarting() {
    return this == RUNNING;
  }
}
