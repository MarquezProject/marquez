package marquez.db;

import java.util.List;
import marquez.api.JobRun;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface JobRunDAO {
  @SqlQuery("SELECT * FROM job_runs WHERE id = :id")
  JobRun findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM job_runs")
  List<JobRun> findAll();
}
