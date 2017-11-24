package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.sb.messenger.persistence.BaseEntity;
import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.toolbox.net.RestCredentials;
import de.sb.toolbox.net.RestJpaLifecycleProvider;

@Path("people")
public class PersonService extends EntityService{
	final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
	final CriteriaBuilder cb = messengerManager.getCriteriaBuilder();
	/*
	@GET
    @Path("/people")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Person getPersonByCriteria( @QueryParam("familyName") String family,
    		@QueryParam("givenName") String given,
    		@QueryParam("email") String email)
 {

		CriteriaBuilder cb = messengerManager.getCriteriaBuilder();
	    CriteriaQuery<Person> q = cb.createQuery(Person.class);
		Root<Person> c = q.from(Person.class);
		q.select(c);
		return null;
		  	
 }
*/	
	
	@PUT
    @Path("people")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public long updatePerson (@HeaderParam("Authorization") final String authentication, 
						 	    @FormParam("identity") long id,
								@FormParam("avatar") final Document avatar,
								@FormParam("familyName") final String family,
						    		@FormParam("givenName") final String given,
						    		@FormParam("email") final String email,
						    		@FormParam("street") final String street,
						    		@FormParam("city") final String city,
						    		@FormParam("postcode") final String postcode,
						    		@FormParam("passwordHash") final byte[] setPassword
    		){
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		
		Person person;
		if (id == 0) {
		 person = (new Person(null));	
		} else {
		 person = messengerManager.find(Person.class, id);
		}

		person.setEmail(email);
		person.getName().setGiven(given);
		person.getName().setFamily(family);
		person.setPasswordHash(setPassword);
		person.getAddress().setCity(city);
		person.getAddress().setPostcode(postcode);
		person.getAddress().setStreet(street);
		person.getAvatar().setContent(avatar.getContent());
		
		messengerManager.getTransaction().begin();
		messengerManager.persist(person);
		messengerManager.getTransaction().commit();
		
		final long identity = person.getIdentity();
		return identity;
	}


	//Entit�ten in Request/Response Body bedeutet was? def. im Methodenkopf
	@GET
	@Path("people/{identity}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person getPersonByID(@HeaderParam("Authorization") final String authentication, @PathParam("identity") final long id) {
		Person person = messengerManager.find(Person.class, id);
		if (person == null) {
			throw new NotFoundException();
		}
		return person;
	}

	
	@GET
	@Path("people/{identity}/messagesAuthored")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public List<Message> getPersonMessagesAuthored(@HeaderParam("Authorization") final String authentication, 
											 @PathParam("identity") final long id) {
		Person person = messengerManager.find(Person.class, id);
		if (person == null) {
			throw new NotFoundException();
		}
		//return Response.ok(person).build();	
		List<Message> messages = new ArrayList<Message>(person.getMessageAuthored());
		messages.sort((a, b) -> {return (int) (a.getIdentity() - b.getIdentity());});
		return messages;
	}
	
	
	@GET
	@Path("people/{identity}/peopleObserving")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public List<Person> getPersonPeopleObserving(@HeaderParam("Authorization") final String authentication, 
											 @PathParam("identity") final long id) {
		Person person = messengerManager.find(Person.class, id);
		if (person == null) {
			throw new NotFoundException();
		}
		
		CriteriaQuery<Person> cq = cb.createQuery(Person.class);
		Root<Person> observers = cq.from(Person.class);
		cq.where(observers.in(person.getPersonObserving()));
		cq.orderBy(cb.asc(observers.get("familyName")), cb.asc(observers.get("givenName")), cb.asc(observers.get("email")));
		
		List<Person> result = messengerManager.createQuery(cq).getResultList();
		return result;
	}

	
	@GET
	@Path("people/{identity}/peopleObserved")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public List<Person> getPersonPeopleObserved(@HeaderParam("Authorization") final String authentication, 
											 @PathParam("identity") final long id) {
		Person person = messengerManager.find(Person.class, id);
		if (person == null) {
			throw new NotFoundException();
		}
		
		CriteriaQuery<Person> cq = cb.createQuery(Person.class);
		Root<Person> observed = cq.from(Person.class);
		cq.where(observed.in(person.getPersonObserved()));
		cq.orderBy(cb.asc(observed.get("familyName")), cb.asc(observed.get("givenName")), cb.asc(observed.get("email")));
		
		List<Person> result = messengerManager.createQuery(cq).getResultList();
		return result;
	}
	
	@GET
	@Path("people/{identity}/avatar")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public List<Person> getPersonAvatar(@HeaderParam("Authorization") final String authentication, 
											 @PathParam("identity") final long id) {
		
		return null;
	}
	
}
