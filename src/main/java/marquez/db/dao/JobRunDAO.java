package marquez.db.dao;

import java.util.List;
import marquez.api.JobRun;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface JobRunDAO {
  @SqlQuery("SELECT * FROM job_runs WHERE id = :id")
  JobRun findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM job_runs")
  List<JobRun> findAll();
}
