/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import marquez.db.models.ColumnLineageNodeData;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DatasetData.class, name = "DATASET"),
  @JsonSubTypes.Type(value = JobData.class, name = "JOB"),
  @JsonSubTypes.Type(value = ColumnLineageNodeData.class, name = "DATASET_FIELD")
})
public interface NodeData {}
