/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobContextDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.LineageDao;
import marquez.db.NamespaceDao;
import marquez.db.OpenLineageDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.RunStateDao;
import marquez.db.SourceDao;
import marquez.db.StreamVersionDao;
import marquez.db.TagDao;

public class DelegatingDaos {
  @RequiredArgsConstructor
  public static class DelegatingDatasetDao implements DatasetDao {
    @Delegate private final DatasetDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingDatasetFieldDao implements DatasetFieldDao {
    @Delegate private final DatasetFieldDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingDatasetVersionDao implements DatasetVersionDao {
    @Delegate private final DatasetVersionDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingJobContextDao implements JobContextDao {
    @Delegate private final JobContextDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingJobDao implements JobDao {
    @Delegate private final JobDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingJobVersionDao implements JobVersionDao {
    @Delegate private final JobVersionDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingNamespaceDao implements NamespaceDao {
    @Delegate private final NamespaceDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingOpenLineageDao implements OpenLineageDao {
    @Delegate private final OpenLineageDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingRunArgsDao implements RunArgsDao {
    @Delegate private final RunArgsDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingRunDao implements RunDao {
    @Delegate private final RunDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingRunStateDao implements RunStateDao {
    @Delegate private final RunStateDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingSourceDao implements SourceDao {
    @Delegate private final SourceDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingStreamVersionDao implements StreamVersionDao {
    @Delegate private final StreamVersionDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingTagDao implements TagDao {
    @Delegate private final TagDao delegate;
  }

  @RequiredArgsConstructor
  public static class DelegatingLineageDao implements LineageDao {
    @Delegate private final LineageDao delegate;
  }
}
