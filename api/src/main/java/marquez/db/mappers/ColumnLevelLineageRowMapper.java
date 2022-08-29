package marquez.db.mappers;

import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.ColumnLevelLineageRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

import static marquez.db.Columns.*;

public class ColumnLevelLineageRowMapper implements RowMapper<ColumnLevelLineageRow> {

    @Override
    public ColumnLevelLineageRow map(@NonNull ResultSet results, @NonNull StatementContext context)
            throws SQLException {
        return new ColumnLevelLineageRow(
                uuidOrThrow(results, Columns.ROW_UUID),
                uuidOrThrow(results, Columns.DATASET_VERSION_UUID),
                stringOrThrow(results, Columns.OUTPUT_COLUMN_NAME),
                stringOrThrow(results, INPUT_FIELD),
                stringOrThrow(results, TRANSFORMATION_DESCRIPTION),
                stringOrThrow(results, TRANSFORMATION_TYPE),
                timestampOrThrow(results, Columns.CREATED_AT),
                timestampOrThrow(results, Columns.UPDATED_AT));
    }
}