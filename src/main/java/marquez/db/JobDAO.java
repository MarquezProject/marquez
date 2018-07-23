package marquez.db;

import marquez.core.Job;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import java.util.List;

public interface JobDAO {

    @SqlQuery("select * from jobs where ID = :id")
    Job findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM jobs")
    List<Job> findAll();

}
