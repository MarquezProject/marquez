package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.urlOrNull;
import static marquez.db.Columns.uuidArrayOrEmpty;
import static marquez.db.Columns.uuidOrThrow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.db.Columns;
import marquez.db.models.JobData;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JobDataMapper implements RowMapper<JobData> {
  Stopwatch sw;

  public JobDataMapper() {
    this.sw = Stopwatch.createUnstarted();
  }

  public JobDataMapper(Stopwatch sw) {
    this.sw = sw;
  }

  public Stopwatch getSw() {
    return sw;
  }

  @Override
  public JobData map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    sw.start();
    try {
      Set<String> columnNames = MapperUtils.getColumnNames(results.getMetaData());
      return new JobData(
          uuidOrThrow(results, Columns.ROW_UUID),
          new JobId(
              NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
              JobName.of(stringOrThrow(results, Columns.NAME))),
          JobType.valueOf(stringOrThrow(results, Columns.TYPE)),
          JobName.of(stringOrThrow(results, Columns.NAME)),
          timestampOrThrow(results, Columns.CREATED_AT),
          timestampOrThrow(results, Columns.UPDATED_AT),
          NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
          ImmutableSet.<DatasetId>of(),
          ImmutableSet.copyOf(uuidArrayOrEmpty(results, Columns.INPUT_UUIDS)),
          ImmutableSet.<DatasetId>of(),
          ImmutableSet.copyOf(uuidArrayOrEmpty(results, Columns.OUTPUT_UUIDS)),
          urlOrNull(results, "current_location"),
          columnNames.contains(Columns.CONTEXT)
              ? toContext(results, Columns.CONTEXT)
              : ImmutableMap.of(),
          stringOrNull(results, Columns.DESCRIPTION),
          null);
    } finally {
      sw.stop();
    }
  }

  public static ImmutableMap<String, String> toContext(ResultSet results, String column)
      throws SQLException {
    if (results.getString(column) == null) {
      return null;
    }
    return Utils.fromJson(
        results.getString(column), new TypeReference<ImmutableMap<String, String>>() {});
  }
}
