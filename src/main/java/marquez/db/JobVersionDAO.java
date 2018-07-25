package marquez.db;

import java.util.List;
import marquez.api.JobVersion;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface JobVersionDAO {
  @SqlQuery("SELECT * FROM job_versions WHERE id = :id")
  JobVersion findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM job_versions")
  List<JobVersion> findAll();
}
