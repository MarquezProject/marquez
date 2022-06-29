/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DatasetData.class, name = "DATASET"),
  @JsonSubTypes.Type(value = JobData.class, name = "JOB")
})
public interface NodeData {}
