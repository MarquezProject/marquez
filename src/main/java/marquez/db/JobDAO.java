package marquez.db;

import java.util.List;
import marquez.api.Job;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface JobDAO {
  @SqlQuery("SELECT * FROM jobs WHERE id = :id")
  Job findById(@Bind("id") Long id);

  @SqlQuery("SELECT * FROM jobs")
  List<Job> findAll();
}
