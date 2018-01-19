package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.persistence.EntityManager;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.sb.toolbox.Copyright;
import de.sb.toolbox.net.RestCredentials;
import de.sb.toolbox.net.RestJpaLifecycleProvider;
import de.sb.messenger.persistence.*;

/**
 * Methods:
 * PUT messages
 * GET messages/{identity}
 * GET messages/{identity}/author
 * GET messages/{identity}/subject
 */
@Path("messages")
@Copyright(year=2017, holders="Team 4")
public class MessageService extends ServiceTest{
	final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");

	private Message getMessage(final long identity) {
		final Message message = messengerManager.find(Message.class, identity);
		if (message == null) throw new ClientErrorException(NOT_FOUND);
		return message;
	}
	
	@PUT
	@Path("messages")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public long putMessage (	@HeaderParam("Authorization") final String authentication, 
							@FormParam("body") final String body,
							@FormParam("authorReference") final long authorReference,
							@FormParam("subjectReference") final long subjectReference) {
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		
		Person author =  messengerManager.find(Person.class, authorReference);
		if (author == null) throw new ClientErrorException(NOT_FOUND);
		
		BaseEntity subject =  messengerManager.find(BaseEntity.class, subjectReference);
		if (subject == null) throw new ClientErrorException(NOT_FOUND);

		messengerManager.getTransaction().begin();
		Message message = new Message(author, subject);
		message.setBody(body);
		messengerManager.persist(message);
		messengerManager.getTransaction().commit();
		
		//evict from 2nd-level cache??
		messengerManager.getEntityManagerFactory().getCache().evict(Person.class, author.getIdentity());
		messengerManager.getEntityManagerFactory().getCache().evict(BaseEntity.class, subject.getIdentity());
		
		final long id = message.getIdentity();
		return id;
	}
	
	
	
	@GET
	@Path("messages/{identity}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Message queryMessage (@HeaderParam("Authorization") final String authentication, 
								@PathParam("identity") final long identity) {
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		return getMessage(identity);
	}

	@GET
	@Path("messages/{identity}/author")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person queryMessageAuthor (@HeaderParam("Authorization") final String authentication,
									 @PathParam("identity") final long identity) {
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		Message message = getMessage(identity);
		Person author = message.getAuthor();
		return author;
	}
	
	@GET
	@Path("messages/{identity}/subject")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public BaseEntity queryMessageSubject (@HeaderParam("Authorization") final String authentication, 
										  @PathParam("identity") final long identity) {
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		Message message = getMessage(identity);
		BaseEntity subject = message.getSubject();
		return subject;
	}
	
	
}
