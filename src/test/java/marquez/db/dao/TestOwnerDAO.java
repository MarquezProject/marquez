package marquez.db.dao;

import org.junit.ClassRule;
import org.junit.Test;

import marquez.api.Owner;
import marquez.db.dao.fixtures.DAOSetup;

public class TestOwnerDAO {

	@ClassRule
	public static final DAOSetup daoSetup = new DAOSetup();

	final OwnerDAO ownerDAO = daoSetup.onDemand(OwnerDAO.class);

	@Test
	public void testUpdateOwner() {
		ownerDAO.insert(new Owner("Amaranta"));
	}
}
