package ch.fork.AdHocRailway.domain;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

public abstract class HibernatePersistence {

	protected EntityManagerFactory	emf;
	protected EntityManager			em;
	private Preferences				preferences;

	public HibernatePersistence() {
		preferences = Preferences.getInstance();
	}

	@SuppressWarnings("unchecked")
	public EntityManager getEntityManager() {
		// Start EntityManagerFactory
		if (emf == null) {
			Map configOverrides = new HashMap();
			configOverrides.put("hibernate.connection.username", preferences
					.getStringValue(PreferencesKeys.DATABASE_USER));
			configOverrides.put("hibernate.connection.password", preferences
					.getStringValue(PreferencesKeys.DATABASE_PWD));
			String host =
					preferences.getStringValue(PreferencesKeys.DATABASE_HOST);
			String database =
					preferences.getStringValue(PreferencesKeys.DATABASE_NAME);
			String url = "jdbc:mysql://" + host + "/" + database;
			configOverrides.put("hibernate.connection.url", url);

			emf =
					Persistence.createEntityManagerFactory("adhocrailway",
							configOverrides);
		}
		// First unit of work
		if (em == null) {
			em = emf.createEntityManager();
			em.getTransaction().begin();
		}
		return em;
	}
}
