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
      "INSERT INTO namespaces(uuid, name, description, current_owner) VALUES(:namespace.name, :namespace.description, :namespace.currentOwner)")
  void insert(@BindBean("namespace") Namespace namespace);

  @SqlQuery("SELECT * FROM namespaces WHERE name = :name")
  Namespace findByName(@Bind("name") String name);

  @SqlQuery("SELECT * FROM namespaces")
  List<Namespace> findAll();
}
