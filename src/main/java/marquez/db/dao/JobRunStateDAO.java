package marquez.db;

import java.util.List;
import marquez.api.JobRunState;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface JobRunStateDAO {
  @SqlQuery("SELECT * FROM job_run_states WHERE id = :id")
  JobRunState findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM job_run_states")
  List<JobRunState> findAll();
}
