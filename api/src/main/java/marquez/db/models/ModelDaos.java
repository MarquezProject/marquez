/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import marquez.db.BaseDao;
import marquez.db.ColumnLineageDao;
import marquez.db.DatasetDao;
import marquez.db.DatasetFacetsDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetSchemaVersionDao;
import marquez.db.DatasetSymlinkDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobDao;
import marquez.db.JobFacetsDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.RunFacetsDao;
import marquez.db.RunStateDao;
import marquez.db.SourceDao;

/**
 * Container for storing all the Dao classes which ensures parent interface methods are called
 * exactly once.
 */
public final class ModelDaos {
  private NamespaceDao namespaceDao = null;
  private DatasetSymlinkDao datasetSymlinkDao = null;
  private DatasetDao datasetDao = null;
  private SourceDao sourceDao = null;
  private DatasetVersionDao datasetVersionDao = null;
  private DatasetSchemaVersionDao datasetSchemaVersionDao = null;
  private DatasetFieldDao datasetFieldDao = null;
  private RunDao runDao = null;
  private DatasetFacetsDao datasetFacetsDao = null;
  private ColumnLineageDao columnLineageDao = null;
  private JobDao jobDao = null;
  private JobFacetsDao jobFacetsDao = null;
  private JobVersionDao jobVersionDao = null;
  private RunArgsDao runArgsDao = null;
  private RunStateDao runStateDao = null;
  private RunFacetsDao runFacetsDao = null;
  private BaseDao baseDao;

  public void initBaseDao(BaseDao baseDao) {
    this.baseDao = baseDao;
  }

  public NamespaceDao getNamespaceDao() {
    if (namespaceDao == null) {
      namespaceDao = baseDao.createNamespaceDao();
    }
    return namespaceDao;
  }

  public DatasetSymlinkDao getDatasetSymlinkDao() {
    if (datasetSymlinkDao == null) {
      datasetSymlinkDao = baseDao.createDatasetSymlinkDao();
    }
    return datasetSymlinkDao;
  }

  public DatasetDao getDatasetDao() {
    if (datasetDao == null) {
      datasetDao = baseDao.createDatasetDao();
    }
    return datasetDao;
  }

  public SourceDao getSourceDao() {
    if (sourceDao == null) {
      sourceDao = baseDao.createSourceDao();
    }
    return sourceDao;
  }

  public DatasetVersionDao getDatasetVersionDao() {
    if (datasetVersionDao == null) {
      datasetVersionDao = baseDao.createDatasetVersionDao();
    }
    return datasetVersionDao;
  }

  public DatasetSchemaVersionDao getDatasetSchemaVersionDao() {
    if (datasetSchemaVersionDao == null) {
      datasetSchemaVersionDao = baseDao.createDatasetSchemaVersionDao();
    }
    return datasetSchemaVersionDao;
  }

  public DatasetFieldDao getDatasetFieldDao() {
    if (datasetFieldDao == null) {
      datasetFieldDao = baseDao.createDatasetFieldDao();
    }
    return datasetFieldDao;
  }

  public RunDao getRunDao() {
    if (runDao == null) {
      runDao = baseDao.createRunDao();
    }
    return runDao;
  }

  public ColumnLineageDao getColumnLineageDao() {
    if (columnLineageDao == null) {
      columnLineageDao = baseDao.createColumnLineageDao();
    }
    return columnLineageDao;
  }

  public DatasetFacetsDao getDatasetFacetsDao() {
    if (datasetFacetsDao == null) {
      datasetFacetsDao = baseDao.createDatasetFacetsDao();
    }
    return datasetFacetsDao;
  }

  public JobDao getJobDao() {
    if (jobDao == null) {
      jobDao = baseDao.createJobDao();
    }
    return jobDao;
  }

  public JobFacetsDao getJobFacetsDao() {
    if (jobFacetsDao == null) {
      jobFacetsDao = baseDao.createJobFacetsDao();
    }
    return jobFacetsDao;
  }

  public RunArgsDao getRunArgsDao() {
    if (runArgsDao == null) {
      runArgsDao = baseDao.createRunArgsDao();
    }
    return runArgsDao;
  }

  public RunStateDao getRunStateDao() {
    if (runStateDao == null) {
      runStateDao = baseDao.createRunStateDao();
    }
    return runStateDao;
  }

  public RunFacetsDao getRunFacetsDao() {
    if (runFacetsDao == null) {
      runFacetsDao = baseDao.createRunFacetsDao();
    }
    return runFacetsDao;
  }

  public JobVersionDao getJobVersionDao() {
    if (jobVersionDao == null) {
      jobVersionDao = baseDao.createJobVersionDao();
    }
    return jobVersionDao;
  }
}
