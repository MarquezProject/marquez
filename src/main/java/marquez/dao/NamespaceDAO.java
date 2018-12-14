package marquez.dao;

import java.util.List;
import marquez.core.models.Namespace;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(NamespaceRow.class)
public interface NamespaceDAO {
  @SqlUpdate(
      "INSERT INTO namespaces(guid, name, description, current_ownership) "
          + "VALUES(:guid, :name, :description, :ownerName) "
          + "ON CONFLICT DO NOTHING")
  void insert(@BindBean Namespace namespace);

  @SqlQuery("SELECT * FROM namespaces WHERE name = :name")
  Namespace find(@Bind("name") String name);

  @SqlQuery("SELECT * FROM namespaces")
  List<Namespace> findAll();

  @SqlQuery("SELECT COUNT(*) > 0 FROM namespaces WHERE name = :name")
  boolean exists(@Bind("name") String name);
}
