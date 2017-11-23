package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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

import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.toolbox.net.RestJpaLifecycleProvider;

@Path("/people")
public class PersonService extends EntityService{
	final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
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
    @Path("/people")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public long updatePerson ( @PathParam("identity") long id,
			@HeaderParam("avatar") Document avatar,
			@HeaderParam("familyName") String family,
    		@HeaderParam("givenName") String given,
    		@HeaderParam("email") String email,
    		@HeaderParam("street") String street,
    		@HeaderParam("city") String city,
    		@HeaderParam("postcode") String postcode,
    		@HeaderParam("passwordHash") byte[] setPassword
    		
    		)
	{

		Person person;

		if (id == 0) {
		 person = (new Person(null));	
		}
		
		else {
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
		
		return person.getIdentity();


	}


//Entitäten in Request/Response Body bedeutet was? def. im Methodenkopf
@GET
@Path("/people/{identity}")
@Produces({ APPLICATION_JSON, APPLICATION_XML })
public Person getPersonByID(@HeaderParam("Authorization") final String authentication, @PathParam("identity") long id) {
	Person person = messengerManager.find(Person.class, id);
	if (person instanceof Person)
	return person;	
	throw new NotFoundException();
}

@GET
@Path("/people/{identity}")
@Produces({ APPLICATION_JSON, APPLICATION_XML })
public Response getPersonByID2(@HeaderParam("Authorization") final String authentication, @PathParam("identity")long id) {
	Person person = messengerManager.find(Person.class, id);
	if (person instanceof Person)
	return Response.ok(person).build();	
	throw new NotFoundException();
}
	
}
