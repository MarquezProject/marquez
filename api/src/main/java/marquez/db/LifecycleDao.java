package marquez.db;

import java.util.List;
import java.util.UUID;
import marquez.db.mappers.LifecycleMapper;
import marquez.service.LifecycleService.Lifecycle;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(LifecycleMapper.class)
public interface LifecycleDao {
  @SqlUpdate(
      """
      INSERT INTO lifecycle_events (uuid, transitioned_at, namespace_name, state, message)
         VALUES (
           :lifecycleEventUuid,
           :lifecycleEvent.transitionedAt,
           :lifecycleEvent.namespace,
           :lifecycleEvent.state,
           :lifecycleEvent.message
         )
      """)
  void insertLifecycleEvent(
      UUID lifecycleEventUuid, @BindBean("lifecycleEvent") Lifecycle.Event lifecycleEvent);

  @SqlQuery(
      """
      SELECT transitioned_at, namespace_name, state, message
        FROM lifecycle_events
       WHERE :namespace IS NULL OR namespace_name = :namespace
       LIMIT :limit
      """)
  List<Lifecycle.Event> findAllLifecycleEvents(String namespace, int limit);
}
