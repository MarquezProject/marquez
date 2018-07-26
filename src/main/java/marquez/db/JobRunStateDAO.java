package marquez.db;

import java.util.List;
import marquez.api.JobRunState;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface JobRunStateDAO {
  @SqlQuery("SELECT * FROM job_run_states WHERE id = :id")
  JobRunState findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM job_run_states")
  List<JobRunState> findAll();
}
