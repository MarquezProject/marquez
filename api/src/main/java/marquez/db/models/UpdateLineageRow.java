/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import marquez.db.JobVersionDao.BagOfJobVersionInfo;

@Getter
@Setter
public class UpdateLineageRow {
  private NamespaceRow namespace;
  private JobRow job;
  private JobContextRow jobContext;
  private RunArgsRow runArgs;
  private RunRow run;
  private RunStateRow runState;
  private Optional<List<DatasetRecord>> inputs;
  private Optional<List<DatasetRecord>> outputs;
  private BagOfJobVersionInfo jobVersionBag;

  @Value
  public static class DatasetRecord {
    DatasetRow datasetRow;
    DatasetVersionRow datasetVersionRow;
    NamespaceRow namespaceRow;
  }
}
