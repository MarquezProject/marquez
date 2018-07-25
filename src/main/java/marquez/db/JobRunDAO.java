package marquez.db;

import java.util.List;
import marquez.api.JobRun;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface JobRunDAO {
  @SqlQuery("SELECT * FROM jobs WHERE id = :id")
  JobRun findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM jobs")
  List<JobRun> findAll();
}
