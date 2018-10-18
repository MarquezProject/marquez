package marquez.resources;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.UUID;
import marquez.api.Owner;
import marquez.dao.OwnerDAO;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class OwnerResourceTest {
  private static final OwnerDAO dao = mock(OwnerDAO.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder().addResource(new OwnerResource(dao)).build();

  private final Owner owner = new Owner("Aureliano");

  @Before
  public void setup() {
    when(dao.findByName(eq("Aureliano"))).thenReturn(owner);
  }

  @After
  public void tearDown() {
    reset(dao);
  }

  @Test
  public void testPostOwner() {
    resources.target("/owners").request().post(entity(owner, APPLICATION_JSON));
    verify(dao).insert(any(UUID.class), eq(owner));
  }

  @Test
  public void testGetOwner() {
    resources.target("/owners/Aureliano").request().get();
    verify(dao).findByName(owner.getName());
    assertEquals(resources.target("/owners/Aureliano").request().get(Owner.class), owner);
  }

  @Test
  public void testDeleteOwner() {
    resources.target("/owners/Aureliano").request().delete();
    verify(dao).delete("Aureliano");
  }

  @Test
  public void testToString() {
    assertEquals("Owner{name=Aureliano}", owner.toString());
  }

  @Test
  public void testHashCode() {
    assertEquals(owner.hashCode(), new Owner("Aureliano").hashCode());
  }

  @Test
  public void testEquals() {
    Owner owner2 = new Owner("Aureliano");
    assertTrue(owner.equals(owner2));
    assertTrue(owner2.equals(owner));
  }
}
