package ch.fork.AdHocRailway.domain;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public abstract class HibernatePersistence {

	protected EntityManager em;

	public HibernatePersistence() {
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("adhocrailway");
		// First unit of work
		this.em = emf.createEntityManager();

	}
}
