package marquez.db.sql;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import marquez.db.models.DbTableInfoRow;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizerFactory;
import org.jdbi.v3.sqlobject.customizer.SqlStatementParameterCustomizer;

public class BindDbTableInfoRowFactory implements SqlStatementCustomizerFactory {
  @Override
  public SqlStatementParameterCustomizer createForParameter(
      Annotation annotation,
      Class<?> sqlObjectType,
      Method method,
      Parameter param,
      int index,
      Type type) {
    return (stmt, obj) -> {
      final DbTableInfoRow dbTableInfoRow = (DbTableInfoRow) obj;
      stmt.bind("uuid", dbTableInfoRow.getUuid().toString())
          .bind("db", dbTableInfoRow.getDb().getValue())
          .bind("db_schema", dbTableInfoRow.getDbSchema().getValue());
    };
  }
}
