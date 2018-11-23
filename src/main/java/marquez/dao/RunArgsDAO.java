package marquez.dao;

import marquez.core.models.RunArgs;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterRowMapper(RunArgsRow.class)
public interface RunArgsDAO extends SqlObject {
  static final Logger LOG = LoggerFactory.getLogger(JobDAO.class);

  @SqlUpdate(
      "INSERT INTO job_run_args(hex_digest, args_json) VALUES (:run_args.hexDigest, :run_args.json)")
  void insert(@BindBean("run_args") RunArgs runArgs);

  @SqlQuery("SELECT COUNT(*) > 0 FROM job_run_args WHERE hex_digest=:digest")
  boolean digestExists(@Bind("digest") String hexDigest);

  @SqlQuery("SELECT * FROM job_run_args WHERE hex_digest=:digest LIMIT 1")
  RunArgs findByDigest(@Bind("digest") String hexDigest);
}
