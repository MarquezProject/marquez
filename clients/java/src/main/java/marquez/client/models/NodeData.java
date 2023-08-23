/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = ColumnLineageNodeData.class)
@JsonSubTypes({
  @JsonSubTypes.Type(DatasetNodeData.class),
  @JsonSubTypes.Type(JobNodeData.class),
  @JsonSubTypes.Type(ColumnLineageNodeData.class),
})
public interface NodeData {}
