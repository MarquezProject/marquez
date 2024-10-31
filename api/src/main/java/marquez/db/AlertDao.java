package marquez.db;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
  List<AlertRow> findAll(String entityType, UUID entityUuid);

  @SqlQuery(
      "SELECT * FROM alerts WHERE entity_type = :entityType AND entity_uuid = :entityUuid AND type = :type limit 1")
  Optional<AlertRow> find(String entityType, UUID entityUuid, String type);

  @SqlUpdate("DELETE FROM alerts WHERE uuid = :uuid")
  void delete(UUID uuid);

  @SqlQuery(
      """
            INSERT INTO alerts (uuid, created_at, entity_type, entity_uuid, type, config)
            VALUES (:uuid, :createdAt, :entityType, :entityUuid, :type, :config)
            ON CONFLICT (entity_type, entity_uuid, type) DO UPDATE
            SET created_at = EXCLUDED.created_at, config = EXCLUDED.config
            RETURNING *""")
  AlertRow upsert(
      UUID uuid,
      Instant createdAt,
      String entityType,
      UUID entityUuid,
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

  @SqlUpdate(
      """
        INSERT INTO notifications (uuid, created_at, alert_uuid, display_name)
        VALUES (:uuid, NOW(), :alertUuid, :displayName)
        """)
  void createNotification(UUID uuid, UUID alertUuid, String displayName);

  @SqlUpdate("UPDATE notifications SET archived_at = NOW() WHERE uuid = :uuid")
  void archiveNotification(UUID uuid);

  @SqlUpdate("UPDATE notifications SET archived_at = NOW() WHERE archived_at IS NULL")
  void archiveAllNotifications();

  @SqlQuery("SELECT COUNT(*) FROM notifications WHERE archived_at IS NULL")
  int countNotifications();
}
