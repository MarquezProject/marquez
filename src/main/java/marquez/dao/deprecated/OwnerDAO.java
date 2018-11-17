package marquez.dao.deprecated;

import java.util.UUID;
import marquez.api.Owner;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(OwnerRow.class)
public interface OwnerDAO {
  @SqlQuery("SELECT * FROM owners WHERE name = :name AND deleted_at IS NULL")
  Owner findByName(@Bind("name") String name);

  @SqlUpdate("INSERT INTO owners (guid, name) VALUES (:guid, :owner.name)")
  void insert(@Bind("guid") final UUID guid, @BindBean("owner") final Owner owner);

  @SqlUpdate("UPDATE owners SET deleted_at=CURRENT_TIMESTAMP WHERE name = :name")
  void delete(@Bind("name") final String name);
}
