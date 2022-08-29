package marquez.db;

import marquez.db.mappers.ColumnLevelLineageMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;

@RegisterRowMapper(ColumnLevelLineageMapper.class)
public interface ColumnLevelLineageDao extends BaseDao {
}
