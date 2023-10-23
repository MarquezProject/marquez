package marquez.db.models;

import marquez.db.BaseDao;
import marquez.db.ColumnLineageDao;
import marquez.db.DatasetDao;
import marquez.db.DatasetFacetsDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetSymlinkDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobDao;
import marquez.db.JobFacetsDao;
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
  private static NamespaceDao namespaceDao = null;
  private static DatasetSymlinkDao datasetSymlinkDao = null;
  private static DatasetDao datasetDao = null;
  private static SourceDao sourceDao = null;
  private static DatasetVersionDao datasetVersionDao = null;
  private static DatasetFieldDao datasetFieldDao = null;
  private static RunDao runDao = null;
  private static DatasetFacetsDao datasetFacetsDao = null;
  private static ColumnLineageDao columnLineageDao = null;
  private static JobDao jobDao = null;
  private static JobFacetsDao jobFacetsDao = null;
  private static RunArgsDao runArgsDao = null;
  private static RunStateDao runStateDao = null;
  private static RunFacetsDao runFacetsDao = null;
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
}
