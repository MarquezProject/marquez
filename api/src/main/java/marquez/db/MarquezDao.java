package marquez.db;

import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;

public interface MarquezDao extends SqlObject {
  @CreateSqlObject
  DatasetDao createDatasetDao();

  @CreateSqlObject
  DatasetFieldDao createDatasetFieldDao();

  @CreateSqlObject
  DatasetVersionDao createDatasetVersionDao();

  @CreateSqlObject
  JobContextDao createJobContextDao();

  @CreateSqlObject
  JobDao createJobDao();

  @CreateSqlObject
  JobVersionDao createJobVersionDao();

  @CreateSqlObject
  NamespaceDao createNamespaceDao();

  @CreateSqlObject
  RunDao createRunDao();

  @CreateSqlObject
  RunArgsDao createRunArgsDao();

  @CreateSqlObject
  RunStateDao createRunStateDao();

  @CreateSqlObject
  SourceDao createSourceDao();

  @CreateSqlObject
  TagDao createTagDao();

  @CreateSqlObject
  StreamVersionDao createStreamVersionDao();
}
