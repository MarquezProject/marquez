package marquez.db.sql;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import marquez.db.models.DbTableVersionRow;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizerFactory;
import org.jdbi.v3.sqlobject.customizer.SqlStatementParameterCustomizer;

public class BindDbTableVersionRowFactory implements SqlStatementCustomizerFactory {
  @Override
  public SqlStatementParameterCustomizer createForParameter(
      Annotation annotation,
      Class<?> sqlObjectType,
      Method method,
      Parameter param,
      int index,
      Type type) {
    return (stmt, obj) -> {
      final DbTableVersionRow dbTableVersionRow = (DbTableVersionRow) obj;
      stmt.bind("uuid", dbTableVersionRow.getUuid().toString())
          .bind("dataset_uuid", dbTableVersionRow.getDatasetUuid().toString())
          .bind("db_table_info_uuid", dbTableVersionRow.getDbTableInfoUuid().toString())
          .bind("db_table", dbTableVersionRow.getDbTable().getValue());
    };
  }
}
