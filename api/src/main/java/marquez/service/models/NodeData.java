/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import marquez.db.models.ColumnLineageNodeData;
import marquez.db.models.InputFieldNodeData;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
  @JsonSubTypes.Type(DatasetData.class),
  @JsonSubTypes.Type(JobData.class),
  @JsonSubTypes.Type(ColumnLineageNodeData.class),
  @JsonSubTypes.Type(InputFieldNodeData.class)
})
public interface NodeData {}
