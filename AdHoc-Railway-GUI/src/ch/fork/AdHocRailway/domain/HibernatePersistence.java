/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.domain;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

public abstract class HibernatePersistence {
	private static Logger logger = Logger.getLogger(HibernatePersistence.class);
	protected static EntityManagerFactory emf;
	protected static EntityManager em;
	private static Preferences preferences = Preferences.getInstance();
	@SuppressWarnings("rawtypes")
	private static Map configOverrides;

	public HibernatePersistence() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setup() {
		if (emf != null) {
			logger.info("Closing existing EntityManagerFactory");
			emf.close();
			emf = null;
		}

		logger.info("Setting up new EntityManagerFactory");
		configOverrides = new HashMap();
		configOverrides.put("hibernate.connection.username",
				preferences.getStringValue(PreferencesKeys.DATABASE_USER));
		configOverrides.put("hibernate.connection.password",
				preferences.getStringValue(PreferencesKeys.DATABASE_PWD));

		String host = preferences.getStringValue(PreferencesKeys.DATABASE_HOST);

		String database = preferences
				.getStringValue(PreferencesKeys.DATABASE_NAME);
		String url = "jdbc:mysql://" + host + "/" + database;
		System.out.println(url);
		configOverrides.put("hibernate.connection.url", url);
	}

	public static void connect() {
		logger.info("Connecting");
		// First unit of work
		if (emf == null) {
			emf = Persistence.createEntityManagerFactory("adhocrailway",
					configOverrides);
		}
		if (em == null) {
			em = emf.createEntityManager();
			em.getTransaction().begin();
		}
	}

	public static void disconnect() {
		logger.info("Disconnecting");
		if (em != null && em.isOpen()) {
			try {
				if (em.getTransaction().isActive())
					em.getTransaction().commit();
			} catch (HibernateException ex) {
				em.getTransaction().rollback();
				em.close();
				throw new TurnoutPersistenceException("Database Error", ex);
			} catch (PersistenceException x) {
				em.getTransaction().rollback();
				em.close();
				throw new TurnoutPersistenceException("Database Error", x);
			}
			em.close();
			em = null;
			emf.close();
			emf = null;
		}
	}

	public static EntityManager getEntityManager() {
		return em;
	}

	public static void flush() throws RuntimeException {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.getTransaction().commit();
		} catch (HibernateException ex) {
			em.getTransaction().rollback();
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new TurnoutPersistenceException("Database Error", ex);
		} catch (PersistenceException x) {
			x.printStackTrace();
			em.getTransaction().rollback();
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new TurnoutPersistenceException("Database Error", x);
		}
		em.getTransaction().begin();
	}
}
