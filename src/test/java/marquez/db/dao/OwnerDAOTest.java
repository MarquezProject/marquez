package marquez.db.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.UUID;
import marquez.api.Owner;
import marquez.db.dao.fixtures.AppWithPostgresRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class OwnerDAOTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  final OwnerDAO ownerDAO = APP.onDemand(OwnerDAO.class);
  final Owner testOwner = new Owner("Amaranta");

  @Before
  public void setUp() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute("DELETE FROM ownerships");
              handle.execute("DELETE FROM owners");
            });
  }

  private static String ownerDeletedAt(String name) {
    return APP.getJDBI()
        .withHandle(
            handle ->
                handle
                    .createQuery("SELECT deleted_at FROM owners WHERE name = :name")
                    .bind("name", name)
                    .mapTo(String.class)
                    .findOnly());
  }

  @Test
  public void testCreateOwner() {
    ownerDAO.insert(UUID.randomUUID(), testOwner);
    Owner o = ownerDAO.findByName(testOwner.getName());
    assertEquals(testOwner.getName(), o.getName());

    // deleted_at should be null in the DB
    String deletedAt = OwnerDAOTest.ownerDeletedAt(testOwner.getName());
    assertEquals(null, deletedAt);

    // owner is fetchable
    assertEquals(testOwner, ownerDAO.findByName(testOwner.getName()));
  }

  @Test
  public void testDeleteOwner() {
    ownerDAO.insert(UUID.randomUUID(), testOwner);
    ownerDAO.delete(testOwner.getName());

    // deleted_at should be set
    String deletedAt = OwnerDAOTest.ownerDeletedAt(testOwner.getName());
    assertNotEquals(null, deletedAt);

    // owner is no longer fetchable
    assertEquals(null, ownerDAO.findByName(testOwner.getName()));
  }
}
