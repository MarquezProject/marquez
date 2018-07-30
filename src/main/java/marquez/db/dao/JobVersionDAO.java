package marquez.db;

import java.util.List;
import marquez.api.JobVersion;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface JobVersionDAO {
  @SqlQuery("SELECT * FROM job_versions WHERE id = :id")
  JobVersion findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM job_versions")
  List<JobVersion> findAll();
}
