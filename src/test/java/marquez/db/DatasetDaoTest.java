package marquez.db;

import static org.junit.Assert.assertEquals;

import marquez.common.models.NamespaceName;
import marquez.db.models.DataSourceRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import marquez.service.models.Generator;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetDaoTest {
  static final Logger logger = LoggerFactory.getLogger(DatasetDaoTest.class);
  private static DatasetDao datasetDAO;
  private static marquez.service.models.Namespace namespace;
  private static NamespaceDao namespaceDAO;

  @ClassRule
  public static final JdbiRule dbRule =
      JdbiRule.embeddedPostgres().withPlugin(new SqlObjectPlugin()).migrateWithFlyway();

  @BeforeClass
  public static void setup() {
    Jdbi jdbi = dbRule.getJdbi();
    namespaceDAO = jdbi.onDemand(NamespaceDao.class);
    datasetDAO = jdbi.onDemand(DatasetDao.class);
  }

  @Before
  public void setupTest() {
    namespace = Generator.genNamespace();
    namespaceDAO.insert(namespace);
  }

  private void insertRandomDataset() {
    DataSourceRow dataSourceRow = Generator.genDataSourceRow();
    DatasetRow datasetRow = Generator.genDatasetRow(namespace.getGuid(), dataSourceRow.getUuid());
    DbTableInfoRow dbTableInfoRow = Generator.genDbTableInfowRow();
    DbTableVersionRow dbTableVersionRow =
        Generator.genDbTableVersionRow(datasetRow.getUuid(), dbTableInfoRow.getUuid());
    datasetDAO.insertAll(dataSourceRow, datasetRow, dbTableInfoRow, dbTableVersionRow);
  }

  @Test
  public void testFindAll() {
    assertEquals(
        0, datasetDAO.findAll(NamespaceName.fromString(namespace.getName()), 10, 0).size());
    insertRandomDataset();
    insertRandomDataset();
    assertEquals(
        2, datasetDAO.findAll(NamespaceName.fromString(namespace.getName()), 10, 0).size());
  }

  @Test
  public void testFindAllWithLimit() {
    final int limit = 1;
    assertEquals(
        0, datasetDAO.findAll(NamespaceName.fromString(namespace.getName()), limit, 0).size());
    insertRandomDataset();
    insertRandomDataset();
    assertEquals(
        limit, datasetDAO.findAll(NamespaceName.fromString(namespace.getName()), limit, 0).size());
  }
}
