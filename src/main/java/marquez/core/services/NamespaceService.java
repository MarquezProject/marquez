package marquez.core.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.models.Namespace;
import marquez.dao.NamespaceDAO;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class NamespaceService {
  private NamespaceDAO namespaceDAO;

  public NamespaceService(NamespaceDAO namespaceDAO) {
    this.namespaceDAO = namespaceDAO;
  }

  public Namespace create(Namespace namespace) throws UnexpectedException {
    try {
      Namespace newNamespace =
          new Namespace(
              UUID.randomUUID(),
              namespace.getName(),
              namespace.getOwnerName(),
              namespace.getDescription());
      namespaceDAO.insert(newNamespace);
      return namespaceDAO.find(newNamespace.getName());
    } catch (UnableToExecuteStatementException e) {
      String err = "error creating namespace";
      log.error(err);
      throw new UnexpectedException();
    }
  }

  public boolean exists(String namespaceName) throws UnexpectedException {
    try {
      return namespaceDAO.exists(namespaceName.toLowerCase());
    } catch (UnableToExecuteStatementException e) {
      String err = "error checking namespace existence";
      log.error(err);
      throw new UnexpectedException();
    }
  }

  public Optional<Namespace> get(String name) throws UnexpectedException {
    try {
      return Optional.ofNullable(namespaceDAO.find(name));
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching namespace";
      log.error(err);
      throw new UnexpectedException();
    }
  }

  public List<Namespace> listNamespaces() throws UnexpectedException {
    try {
      return namespaceDAO.findAll();
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching list of namespaces";
      log.error(err);
      throw new UnexpectedException();
    }
  }
}
