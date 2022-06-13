/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static marquez.common.models.DatasetType.DB_TABLE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.RunId;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTableMeta extends DatasetMeta {
  public DbTableMeta(
      final DatasetName physicalName,
      final SourceName sourceName,
      @Nullable final ImmutableList<Field> fields,
      @Nullable final ImmutableSet<TagName> tags,
      @Nullable final String description,
      @Nullable final RunId runId) {
    super(DB_TABLE, physicalName, sourceName, fields, tags, description, runId);
  }
}
