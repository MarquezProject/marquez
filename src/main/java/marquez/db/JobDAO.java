package marquez.db;

import java.util.List;
import marquez.api.Job;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface JobDAO {
  @SqlQuery("SELECT * FROM jobs WHERE id = :id")
  Job findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM jobs")
  List<Job> findAll();
}
