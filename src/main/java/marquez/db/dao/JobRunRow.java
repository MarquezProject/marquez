package marquez.db.dao;


import marquez.api.Job;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

final class JobRunRow implements RowMapper<Job> {
    @Override
    public Job map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        throw new UnsupportedOperationException();
    }
}