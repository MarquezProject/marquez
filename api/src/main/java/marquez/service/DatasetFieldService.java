package marquez.service;

import lombok.extern.slf4j.Slf4j;
import marquez.db.BaseDao;

@Slf4j
public class DatasetFieldService extends DelegatingDaos.DelegatingDatasetFieldDao {

  public DatasetFieldService(BaseDao baseDao) {
    super(baseDao.createDatasetFieldDao());
  }
}
