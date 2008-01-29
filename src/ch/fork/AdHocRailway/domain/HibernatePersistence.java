package ch.fork.AdHocRailway.domain;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public abstract class HibernatePersistence {

	protected static EntityManagerFactory emf;
	protected static EntityManager em;
	
	public HibernatePersistence() {

	}

	public static EntityManager getEntityManager() {
		// Start EntityManagerFactory
		if (emf == null)
			emf = Persistence.createEntityManagerFactory("adhocrailway");
		// First unit of work
		if(em == null) {
			em = emf.createEntityManager();
			em.getTransaction().begin();
		}
		return em;
	}
}
