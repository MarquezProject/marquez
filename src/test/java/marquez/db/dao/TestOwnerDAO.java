package marquez.db.dao;

import marquez.api.Owner;
import marquez.db.dao.fixtures.DAOSetup;
import org.junit.ClassRule;
import org.junit.Test;

public class TestOwnerDAO {

  @ClassRule public static final DAOSetup daoSetup = new DAOSetup();

  final OwnerDAO ownerDAO = daoSetup.onDemand(OwnerDAO.class);

  @Test
  public void testUpdateOwner() {
    ownerDAO.insert(new Owner("Amaranta"));
    // TODO: getOwner
  }
}
