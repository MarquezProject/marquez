package marquez.db;

import marquez.core.models.RunArgs;
import marquez.db.mappers.JobRunArgsRowMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(JobRunArgsRowMapper.class)
public interface JobRunArgsDao {
  @SqlUpdate("INSERT INTO job_run_args(hex_digest, args_json) VALUES (:hexDigest, :json)")
  void insert(@BindBean RunArgs runArgs);

  @SqlQuery("SELECT COUNT(*) > 0 FROM job_run_args WHERE hex_digest=:digest")
  boolean digestExists(@Bind("digest") String hexDigest);

  @SqlQuery("SELECT * FROM job_run_args WHERE hex_digest=:digest LIMIT 1")
  RunArgs findByDigest(@Bind("digest") String hexDigest);
}
