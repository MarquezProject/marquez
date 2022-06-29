/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.NonNull;
import lombok.Value;

@Value
public class RunArgsRow {
  @NonNull UUID uuid;
  @NonNull Instant createdAt;
  @NonNull String args;
  @NonNull String checksum;
}
