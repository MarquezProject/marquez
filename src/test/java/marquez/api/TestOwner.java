package marquez.api;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import marquez.owner.resource.model.Owner;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

import io.dropwizard.testing.junit.ResourceTestRule;
import marquez.owner.repository.OwnerDAO;
import marquez.owner.resource.OwnerResource;

public class TestOwner {
	private static final OwnerDAO dao = mock(OwnerDAO.class);

	@ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new OwnerResource(dao))
            .build();

	private final Owner owner = new Owner("Aureliano");

    @After
    public void tearDown(){
        reset(dao);
    }

    @Test
    public void testPostOwner() {
        resources.target("/owners").request().post(entity(owner, APPLICATION_JSON));
        verify(dao).insert(owner);
    }
}
