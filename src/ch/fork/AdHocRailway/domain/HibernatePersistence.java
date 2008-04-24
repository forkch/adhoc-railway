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

import org.hibernate.HibernateException;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

public abstract class HibernatePersistence {

	protected static EntityManagerFactory	emf;
	protected static EntityManager			em;
	private static Preferences				preferences	= Preferences
																.getInstance();

	public HibernatePersistence() {
	}

	public static void connect() {
		// Start EntityManagerFactory
		Map configOverrides = new HashMap();
		configOverrides.put("hibernate.connection.username", preferences
				.getStringValue(PreferencesKeys.DATABASE_USER));
		configOverrides.put("hibernate.connection.password", preferences
				.getStringValue(PreferencesKeys.DATABASE_PWD));
		String host = preferences.getStringValue(PreferencesKeys.DATABASE_HOST);
		String database = preferences
				.getStringValue(PreferencesKeys.DATABASE_NAME);
		String url = "jdbc:mysql://" + host + "/" + database;
		configOverrides.put("hibernate.connection.url", url);

		emf = Persistence.createEntityManagerFactory("adhocrailway",
				configOverrides);

	}
	
	public static void disconnect() {
		em.close();
		emf.close();
		em = null;
		emf = null;
	}

	@SuppressWarnings("unchecked")
	public static EntityManager getEntityManager() {

		// First unit of work
		if (em == null) {
			em = emf.createEntityManager();
			em.getTransaction().begin();
		}
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
			em.getTransaction().rollback();
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new TurnoutPersistenceException("Database Error", x);
		}
		em.getTransaction().begin();
	}
}
