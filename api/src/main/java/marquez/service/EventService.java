package marquez.service;

import lombok.NonNull;
import marquez.db.BaseDao;

public class EventService extends DelegatingDaos.DelegatingEventDao {
  public EventService(@NonNull BaseDao baseDao) {
    super(baseDao.createEventDao());
  }
}
