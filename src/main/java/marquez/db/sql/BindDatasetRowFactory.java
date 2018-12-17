package marquez.db.sql;

import static marquez.common.models.Description.NO_DESCRIPTION;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import marquez.db.models.DatasetRow;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizerFactory;
import org.jdbi.v3.sqlobject.customizer.SqlStatementParameterCustomizer;

public class BindDatasetRowFactory implements SqlStatementCustomizerFactory {
  @Override
  public SqlStatementParameterCustomizer createForParameter(
      Annotation annotation,
      Class<?> sqlObjectType,
      Method method,
      Parameter param,
      int index,
      Type type) {
    return (stmt, obj) -> {
      final DatasetRow datasetRow = (DatasetRow) obj;
      stmt.bind("uuid", datasetRow.getUuid().toString())
          .bind("namespace_uuid", datasetRow.getNamespaceUuid().toString())
          .bind("datasource_uuid", datasetRow.getDataSourceUuid().toString())
          .bind("urn", datasetRow.getUrn().getValue())
          .bind("description", datasetRow.getDescription().orElse(NO_DESCRIPTION).getValue());
    };
  }
}
