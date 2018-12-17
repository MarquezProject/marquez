package marquez.db.sql;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import marquez.db.models.DataSourceRow;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizerFactory;
import org.jdbi.v3.sqlobject.customizer.SqlStatementParameterCustomizer;

public class BindDataSourceRowFactory implements SqlStatementCustomizerFactory {
  @Override
  public SqlStatementParameterCustomizer createForParameter(
      Annotation annotation,
      Class<?> sqlObjectType,
      Method method,
      Parameter param,
      int index,
      Type type) {
    return (stmt, obj) -> {
      final DataSourceRow dataSourceRow = (DataSourceRow) obj;
      stmt.bind("uuid", dataSourceRow.getUuid().toString())
          .bind("data_sources", dataSourceRow.getDataSource().getValue())
          .bind("connection_url", dataSourceRow.getConnectionUrl().getRawValue());
    };
  }
}
