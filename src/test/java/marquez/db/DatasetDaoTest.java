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
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetDaoTest {
  static final Logger logger = LoggerFactory.getLogger(DatasetDaoTest.class);
  private DatasetDao datasetDAO;
  private marquez.service.models.Namespace namespace = Generator.genNamespace();
  private NamespaceDao namespaceDAO;

  @Rule
  public final JdbiRule dbRule =
      JdbiRule.embeddedPostgres().withPlugin(new SqlObjectPlugin()).migrateWithFlyway();

  @Before
  public void setup() {
    Jdbi jdbi = dbRule.getJdbi();
    datasetDAO = jdbi.onDemand(DatasetDao.class);
    namespaceDAO = jdbi.onDemand(NamespaceDao.class);
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
  public void testFindAll() throws Exception {
    assertEquals(
        0, datasetDAO.findAll(NamespaceName.fromString(namespace.getName()), 10, 0).size());
    insertRandomDataset();
    insertRandomDataset();
    assertEquals(
        2, datasetDAO.findAll(NamespaceName.fromString(namespace.getName()), 10, 0).size());
  }
}
