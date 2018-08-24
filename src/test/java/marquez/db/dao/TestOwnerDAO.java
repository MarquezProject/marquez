package marquez.db.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import marquez.api.Owner;
import marquez.db.dao.fixtures.DAOSetup;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class TestOwnerDAO {

  @ClassRule public static final DAOSetup daoSetup = new DAOSetup();

  final OwnerDAO ownerDAO = daoSetup.onDemand(OwnerDAO.class);
  final Owner testOwner = new Owner("Amaranta");

  @Before
  public void setUp() {
    Jdbi jdbi = daoSetup.getJDBI();
    jdbi.useHandle(
        handle -> {
          handle.execute("DELETE FROM ownerships");
          handle.execute("DELETE FROM owners");
        });
  }

  private static String ownerDeletedAt(String name) {
    List<String> deletedAts =
        daoSetup
            .getJDBI()
            .withHandle(
                handle ->
                    handle
                        .createQuery("SELECT deleted_at FROM owners WHERE name = :name")
                        .bind("name", name)
                        .mapTo(String.class)
                        .list());
    return deletedAts.get(0);
  }

  @Test
  public void testUpdateOwner() {
    ownerDAO.insert(testOwner);
    Owner o = ownerDAO.findByName(testOwner.getName());
    assertEquals(testOwner.getName(), o.getName());

    String deletedAt = TestOwnerDAO.ownerDeletedAt(testOwner.getName());
    assertEquals(null, deletedAt);
  }

  @Test
  public void testDeleteOwner() {
    ownerDAO.insert(testOwner);
    ownerDAO.delete(testOwner.getName());

    String deletedAt = TestOwnerDAO.ownerDeletedAt(testOwner.getName());
    assertNotEquals(null, deletedAt);
  }
}
