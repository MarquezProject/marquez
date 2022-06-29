/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import javax.ws.rs.NotFoundException;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobId;
import marquez.common.models.JobVersionId;
import marquez.common.models.RunId;

public class NodeIdNotFoundException extends NotFoundException {
  public NodeIdNotFoundException(String message) {
    super(message);
  }

  public NodeIdNotFoundException(DatasetVersionId versionId) {
    super(String.format("Failed to get dataset version: %s", versionId.getName().getValue()));
  }

  public NodeIdNotFoundException(JobVersionId versionId) {
    super(String.format("Failed to get job version: %s", versionId.getName().getValue()));
  }

  public NodeIdNotFoundException(RunId runId) {
    super(String.format("Failed to get run: %s", runId.getValue()));
  }

  public NodeIdNotFoundException(DatasetId datasetId) {
    super(String.format("Failed to get dataset: %s", datasetId.getName().getValue()));
  }

  public NodeIdNotFoundException(JobId jobId) {
    super(String.format("Failed to get job: %s", jobId.getName().getValue()));
  }
}
