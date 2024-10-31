package marquez.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.AlertDao;
import marquez.db.models.AlertRow;
import marquez.db.models.NotificationRow;

@Slf4j
public class AlertService {
  private final AlertDao alertDao;

  public AlertService(@NonNull final AlertDao alertDao) {
    this.alertDao = alertDao;
  }

  public List<AlertRow> findAll(String entityType, String entityUuid) {
    return alertDao.findAll(entityType, UUID.fromString(entityUuid));
  }

  public AlertRow upsert(String entityType, String entityUuid, String type, JsonNode config) {
    return alertDao.upsert(
        UUID.randomUUID(), Instant.now(), entityType, UUID.fromString(entityUuid), type, config);
  }

  public void delete(String entityType, String entityUuid, String type) {
    alertDao.delete(entityType, UUID.fromString(entityUuid), type);
  }

  public List<NotificationRow> listNotifications() {
    return alertDao.listNotifications();
  }

  public void archiveNotification(UUID id) {
    alertDao.archiveNotification(id);
  }

  public void archiveAllNotifications() {
    alertDao.archiveAllNotifications();
  }

  public int countNotifications() {
    return alertDao.countNotifications();
  }
}
