package de.sb.messenger.rest;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Person;
import de.sb.toolbox.net.HttpCredentials;
import de.sb.toolbox.net.RestCredentials;
import de.sb.toolbox.net.RestJpaLifecycleProvider;


/**
 * Facade interface for HTTP authentication purposes.
 * TODO: Rename to "Authenticator" before use.
 */

public interface Authenticator {

	/**
	 * Returns the authenticated requester (a person) for the given HTTP Basic authentication credentials.
	 * A SHA-256 hash-code is calculated for the contained password, and uses it in conjunction with the
	 * user email to query and return a suitable Person entity from the database.
	 * @param credentials the HTTP Basic authentication credentials
	 * @return the authenticated requestor
	 * @throws NotAuthorizedException (HTTP 401) if the given credentials are invalid
	 * @throws PersistenceException (HTTP 500) if there is a problem with the persistence layer
	 * @throws IllegalStateException (HTTP 500) if the entity manager associated with the current
	 *         thread is not open
	 * @throws NullPointerException (HTTP 500) if the given credentials are {@code null}
	 */
	@SuppressWarnings("unused")
	static public Person authenticate ( final HttpCredentials.Basic credentials) throws NotAuthorizedException, PersistenceException, IllegalStateException, NullPointerException {
		final String pql = "select p from Person as p where p.email = :email";
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final String email = credentials.getUsername();
		// TODO: Add JPA authentication by calculating the password hash from the given password,
		// creating a query using the constant above, and returning the person if it matches the
		// password hash. If there is none, or if it fails the password hash check, then throw
		// NotAuthorizedException("Basic"). Note that this exception type is a specialized Subclass
		// of ClientErrorException that is capable of storing an authentication challenge.
		if (credentials==null) throw new NullPointerException();
		
		 TypedQuery<Person> query = messengerManager.createQuery(pql, Person.class);
		 query.setParameter("email", email);
		 Person requestor;
		 try {
			 requestor = query.getSingleResult();
		 } catch (Exception e) {
			 throw new javax.ws.rs.NotFoundException();
		 }
		
		if (Arrays.equals(Person.passwordHash(credentials.getPassword()), requestor.getPasswordHash()) ) { //username = password?
			return requestor;
		}
		
		throw new NotAuthorizedException("Basic");
	}
}