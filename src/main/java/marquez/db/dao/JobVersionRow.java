package marquez.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

import marquez.api.JobVersion;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JobVersionRow implements RowMapper<JobVersion> {
  @Override
  public JobVersion map(ResultSet rs, StatementContext ctx) throws SQLException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSS-XX");

    Timestamp createdAt;
    Timestamp updatedAt;
    try{
       createdAt = new Timestamp(dateFormat.parse(rs.getString("created_at")).getTime());
       updatedAt = new Timestamp(dateFormat.parse(rs.getString("updated_at")).getTime());
    } catch (ParseException e){
      // TODO: log caught exception here
      createdAt = null;
      updatedAt = null;
    }

    return new JobVersion(
        UUID.fromString(rs.getString("guid")), 
        UUID.fromString(rs.getString("job_guid")),
        rs.getString("uri"), 
        UUID.fromString(rs.getString("version")),
        null, // TODO: populate eventually
        createdAt,
        updatedAt
        );
  }
}
