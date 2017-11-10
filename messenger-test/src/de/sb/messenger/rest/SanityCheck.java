package de.sb.messenger.rest;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class SanityCheck {

	public static void main (String[] args) {
		final EntityManagerFactory emf = Persistence.createEntityManagerFactory("messenger");
		final EntityManager em = emf.createEntityManager();
		final Query query = em.createQuery("select p from Person as p");
		System.out.println(query.getResultList());
	}
}
