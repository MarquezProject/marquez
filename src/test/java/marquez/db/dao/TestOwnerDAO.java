package marquez.db.dao;

import org.jdbi.v3.core.Jdbi;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.Before;

import marquez.api.Owner;
import marquez.db.dao.fixtures.DAOSetup;

public class TestOwnerDAO {

	@ClassRule
	public static final DAOSetup daoSetup = new DAOSetup();

	final OwnerDAO ownerDAO = daoSetup.onDemand(OwnerDAO.class);

	@Before
	public void setUp() {
		Jdbi jdbi = daoSetup.getJDBI();
		jdbi.useHandle(handle -> {
			handle.execute("DELETE FROM ownerships");
			handle.execute("DELETE FROM owners");
		});
	}

	@Test
	public void testUpdateOwner() {
		String name = "Amaranta";
		ownerDAO.insert(new Owner(name));
		Owner o = ownerDAO.findByName(name);
		assertEquals(name, o.getName());
	}
}
