package marquez.db;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import marquez.db.mappers.AlertMapper;
import marquez.db.mappers.NotificationMapper;
import marquez.db.models.AlertRow;
import marquez.db.models.NotificationRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(AlertMapper.class)
@RegisterRowMapper(NotificationMapper.class)
public interface AlertDao {

  @SqlQuery("SELECT * FROM alerts WHERE entity_type = :entityType AND entity_uuid = :entityUuid")
  List<AlertRow> findAll(String entityType, String entityUuid);

  @SqlUpdate(
      "DELETE FROM alerts WHERE entity_type = :entityType AND entity_uuid = :entityUuid AND type = :type")
  void delete(String entityType, String entityUuid, String type);

  @SqlQuery(
      """
            INSERT INTO alerts (id, created_at, entity_type, entity_uuid, type, config)
            VALUES (:id, :createdAt, :entityType, :entityUuid, :type, :config)
            ON CONFLICT (entity_type, entity_uuid, type) DO UPDATE
            SET created_at = EXCLUDED.created_at, config = EXCLUDED.config
            RETURNING *""")
  AlertRow upsert(
      UUID id,
      Instant createdAt,
      String entityType,
      String entityUuid,
      String type,
      JsonNode config);

  @SqlQuery(
      """
        SELECT n.uuid, n.created_at, n.archived_at, a.entity_type, a.entity_uuid, a.type, a.config
        FROM notifications AS n INNER JOIN alerts AS a ON n.alert_uuid = a.uuid
        WHERE archived_at IS NULL
        ORDER BY n.created_at DESC
        """)
  List<NotificationRow> listNotifications();

  @SqlUpdate("UPDATE notifications SET archived_at = NOW() WHERE id = :id")
  void archiveNotification(UUID id);

  @SqlUpdate("UPDATE notifications SET archived_at = NOW() WHERE archived_at IS NULL")
  void archiveAllNotifications();

  @SqlQuery("SELECT COUNT(*) FROM notifications WHERE archived_at IS NULL")
  int countNotifications();
}
