package marquez.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import marquez.db.NamespaceDao;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.models.Namespace;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class NamespaceService {
  private NamespaceDao namespaceDao;

  public NamespaceService(NamespaceDao namespaceDao) {
    this.namespaceDao = namespaceDao;
  }

  public Namespace create(Namespace namespace) throws UnexpectedException {
    try {
      Namespace newNamespace =
          new Namespace(
              UUID.randomUUID(),
              namespace.getName(),
              namespace.getOwnerName(),
              namespace.getDescription());
      namespaceDao.insert(newNamespace);
      return namespaceDao.find(newNamespace.getName());
    } catch (UnableToExecuteStatementException e) {
      String err = "error creating namespace";
      log.error(err);
      throw new UnexpectedException();
    }
  }

  public boolean exists(String namespaceName) throws UnexpectedException {
    try {
      return namespaceDao.exists(namespaceName.toLowerCase());
    } catch (UnableToExecuteStatementException e) {
      String err = "error checking namespace existence";
      log.error(err);
      throw new UnexpectedException();
    }
  }

  public Optional<Namespace> get(String name) throws UnexpectedException {
    try {
      return Optional.ofNullable(namespaceDao.find(name));
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching namespace";
      log.error(err);
      throw new UnexpectedException();
    }
  }

  public List<Namespace> listNamespaces() throws UnexpectedException {
    try {
      return namespaceDao.findAll();
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching list of namespaces";
      log.error(err);
      throw new UnexpectedException();
    }
  }
}
