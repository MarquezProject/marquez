package marquez.db.mappers;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapperUtils {

  public static Set<String> getColumnNames(ResultSetMetaData metaData) {
    try {
      Set<String> columns = new HashSet<>();
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        columns.add(metaData.getColumnName(i));
      }
      return columns;
    } catch (SQLException e) {
      log.error("Unable to get column names", e);
      throw new RuntimeException(e);
    }
  }
}
