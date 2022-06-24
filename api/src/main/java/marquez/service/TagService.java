/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.BaseDao;
import marquez.service.models.Tag;

@Slf4j
public class TagService extends DelegatingDaos.DelegatingTagDao {

  public TagService(@NonNull final BaseDao dao) {
    super(dao.createTagDao());
  }

  public void init(@NonNull ImmutableSet<Tag> tags) {
    for (final Tag tag : tags) {
      upsert(tag);
    }
  }
}
