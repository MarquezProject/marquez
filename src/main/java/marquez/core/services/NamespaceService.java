package marquez.core.services;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.models.Namespace;

public class NamespaceService {

  public Namespace create(String name, String ownerName, String description)
      throws UnexpectedException {
    // TODO: Call to DAO layer to insert into DB
    return new Namespace(
        UUID.randomUUID(), Timestamp.from(Instant.now()), name, ownerName, description);
  }

  public Optional<Namespace> get(String name) throws UnexpectedException {
    // TODO: Call to DAO layer to get a Namespace
    return Optional.empty();
  }

  public List listNamespaces() throws UnexpectedException {
    // TODO: Call to DAO layer to get all Namespaces
    return Collections.emptyList();
  }
}
