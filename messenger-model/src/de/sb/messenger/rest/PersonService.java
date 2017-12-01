package de.sb.messenger.rest;

import static de.sb.messenger.persistence.Person.Group.ADMIN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
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
import de.sb.toolbox.Copyright;
import de.sb.toolbox.net.RestCredentials;
import de.sb.toolbox.net.RestJpaLifecycleProvider;

/* Step-by-step guide 
 * Run ServiceTest as JavaApplication
 * http://localhost:8001/services/authentication/basic
 * Authenticate yourself with user: sascha pw: sascha
 * 
 * 
 */

@Path("people")
@Copyright(year=2017, holders="Team 4")

public class PersonService{


	// http://localhost:8001/services/people/people?familyName=Bergmann&givenName=Ines&...

	@GET
	//@Path("people")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person[] getPersonByCriteria(@HeaderParam("Authorization") final String authentication,
			@Size(max=31)@QueryParam("familyName") final String family, @Size(max=31)@QueryParam("givenName") final String given,
			@Size(max=63)@QueryParam("street") final String street, @Size(max=63) @QueryParam("city") final String city,
			@Size(max=15) @QueryParam("postcode") final String postcode, @Pattern(regexp="^.+@.+$") @QueryParam("email") final String email,
			@QueryParam("timestamp") long creationTimestamp

	) {

		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		TypedQuery<Person> query = messengerManager.createQuery("SELECT p FROM Person p WHERE"
				// +":creationTimestamp is null OR p.creationTimeStamp BETWEEN
				// creationTimestamp"
				+ ":email is null OR p.email =:email  AND" + ":familyName is null OR p.familyName  =:familyName AND"
				+ ":givenName  is null OR p.givenName =:givenName AND" + ":street  is null OR p.street =:street AND"
				+ ":city  is null OR p.city =:city AND" + ":postcode is null OR p.postcode =:postcode"
				+ "ORDER BY familyName, givenName, email", Person.class);
		List<Person> result = query.getResultList();
		
		if (result == null) throw new ClientErrorException(404);
		//throw new NotFoundException();
		
		return result.toArray(new Person[0]);
	}

	@PUT
	//@Path("people")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public long updatePerson(@HeaderParam("Authorization") final String authentication, @Valid Person template,
			@NotNull @HeaderParam("SetPassword") final String setPassword) {
		
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		
		final Person requester = Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		if (requester.getGroup() != ADMIN) throw new ClientErrorException(FORBIDDEN);

		final boolean insertMode = (template.getIdentity() == 0); // if template is zero it is true
		Person person;

		if (insertMode) {
			person = (new Person(null));
		} else {
			person = messengerManager.find(Person.class, template.getIdentity());
		}

		person.setEmail(template.getEmail());
		person.getName().setGiven(template.getName().getGiven());
		person.getName().setFamily(template.getName().getFamily());
		person.setPasswordHash(Person.passwordHash(setPassword));// static methods are bound by class, not object
		person.getAddress().setCity(template.getAddress().getCity());
		person.getAddress().setPostcode(template.getAddress().getPostcode());
		person.getAddress().setStreet(template.getAddress().getStreet());
		person.getAvatar().setContent(template.getAvatar().getContent());

		try {
			messengerManager.persist(person);
			messengerManager.getTransaction().commit();
			
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
			
		} finally {
			messengerManager.getTransaction().begin();
		}

		return person.getIdentity();
	}
	
	// http://localhost:8001/services/people/3/
	@GET
	@Path("requester")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person getRequester( @HeaderParam("Authorization") final String authentication) {
		final Person requester = Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));	
		return requester;
	}
	

	// http://localhost:8001/services/people/3/
	@GET
	@Path("{identity}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person getPersonByID( //@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id) {
		EntityManagerFactory emf  = Persistence.createEntityManagerFactory( "messenger" );
		final EntityManager messengerManager = emf.createEntityManager();
		//= RestJpaLifecycleProvider.entityManager("messenger");
	//	Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final Person person = messengerManager.find(Person.class, id);
		if (person == null)	throw new NotFoundException();
		return person;
	}

	// http://localhost:8001/services/people/3/messagesAuthored
	@GET
	@Path("{identity}/messagesAuthored")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Message[] getPersonMessagesAuthored(@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id) {

		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		Person person = messengerManager.find(Person.class, id);
		if (person == null)
			throw new NotFoundException();

		final Message[] messages = person.getMessageAuthored().toArray(new Message[0]);
		// [0] to implicitly tell its a message object no f** clue how big the array
		// suppose to be - so make it as big as it needs to be
		Arrays.sort(messages); // comparable was implemented for BaseEntity --> id as the criteria for the
								// natural order
		return messages;
	}

	// http://localhost:8001/services/people/3/peopleObserving
	@GET
	@Path("people/{identity}/peopleObserving")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person[] getPersonPeopleObserving(@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id) {

		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final Person person = messengerManager.find(Person.class, id);
		if (person == null)
			throw new NotFoundException();

		Person[] people = person.getPersonObserved().toArray(new Person[0]);
		Arrays.sort(people, Comparator.comparing((Person p) -> p.getName().getFamily())
				.thenComparing(p -> p.getName().getGiven()).thenComparing(p -> p.getEmail()));

		return people;
	}

	// http://localhost:8001/services/people/3/peopleObserved
	@GET
	@Path("{identity}/peopleObserved")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person[] getPersonPeopleObserved(@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id) {

		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final Person person = messengerManager.find(Person.class, id);
		if (person == null) {
			throw new NotFoundException();
		}

		Person[] people = person.getPersonObserving().toArray(new Person[0]);
		Arrays.sort(people, Comparator.comparing((Person p) -> p.getName().getFamily())
				.thenComparing(p -> p.getName().getGiven()).thenComparing(p -> p.getEmail()));

		return people;

	}

	/*
	 * PUT /people/{identity}/peopleObserved: Updates the person matching the given
	 * identity to monitor the people matching the form-supplied collection of
	 * person identities. Hint: Make sure all people whose Mirror-Relations change
	 * due to this operation are evicted from the 2nd-level cache!
	 */

	@PUT
	@Path("{identity}/peopleObserved")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public void updatePersonPeopleObserved(@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id, @NotNull @FormParam("peopleObserved") final Set<Long> peopleObservedIds) {

		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final Person person = messengerManager.find(Person.class, id);

		if (person == null)
			throw new NotFoundException();

		Map<Long, Long> map = new HashMap<Long, Long>(person.getPersonObserved().size());

		for (Person p : person.getPersonObserved()) {
			map.put(p.getIdentity(), p.getIdentity());
		}

		Person wanted;

		for (long identity : peopleObservedIds) {
			wanted = messengerManager.find(Person.class, identity);
			if (wanted == null) throw new NotFoundException();

			if (!(peopleObservedIds.contains((map.get(identity))))) {
				person.getPersonObserved().remove(wanted);
				messengerManager.getEntityManagerFactory().getCache().evict(Person.class, identity);
			}

			else if (!(person.getPersonObserved().contains(wanted))) {
				person.getPersonObserved().add(wanted);
				messengerManager.getEntityManagerFactory().getCache().evict(Person.class, identity);
			}

		}

		messengerManager.getEntityManagerFactory().getCache().evict(Person.class, id);
		messengerManager.persist(person);
		try {
			messengerManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
		} finally {
			messengerManager.getTransaction().begin();
		}

	}

	/*
	 * Returns the avatar content of the person matching the given identity (plus
	 * it's content type as part of the HTTP response header). Hint:
	 * Use @Produces(WILDCARD) to declare production of an a priori unknown media
	 * type, and return an instance of Result that contains both the document's
	 * media type and it's content.
	 */

	// http://localhost:8001/services/people/people/3/avatar
	@GET
	@Path("{identity}/avatar")
	@Produces(MediaType.WILDCARD)
	public Response getPersonAvatar(@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id) {

		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final Person person = messengerManager.find(Person.class, id);
		if (person == null)
			throw new NotFoundException();

		byte[] content = person.getAvatar().getContent();
		String contentType = person.getAvatar().getContentType();

		Response result;
		if (content != null) {
			result = Response.ok(content).header("contentType", contentType).build();

		} else {
			result = Response.noContent().build();
		}
		return result;
	}

	/*
	 * Updates the person's avatar (owner only), with the document content being
	 * passed within the HTTP request body, and the media type passed as
	 * Header-Field �Content-type�. If the given content is empty, the person's
	 * avatar shall be set to the default document (identity=1). Otherwise, if a
	 * document matching the media hash of the given content already exists, then
	 * this document shall become the person's avatar. Otherwise, the given content
	 * and content-type is used to create a new document which becomes the person's
	 * avatar. Hint: Use
	 * 
	 * @Consumes(WILDCARD) to declare consumption of an a priori unknown media type.
	 */

	@PUT
	@Path("{identity}/avatar")
	@Consumes(MediaType.WILDCARD)
	public Document updatePersonAvatar(@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id, @NotNull @Pattern(regexp="^[a-z]+\\/[a-z\\.\\+\\-]+$") @HeaderParam("Content-type") final String contentType,
			@Valid Document avatar) {
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		//final Person requester = Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		//if (requester.getGroup() != ADMIN) throw new ClientErrorException(FORBIDDEN);
		
		final Person person = messengerManager.find(Person.class, id);
		final Document defaultAvatar = messengerManager.find(Document.class, 1l);

		if (person == null)
			throw new NotFoundException();
	//	throw new ClientErrorException(NOT_FOUND);

		if (avatar == null) {
			person.getAvatar().setContent(defaultAvatar.getContent()); // identity = 1 ??
			person.getAvatar().setContentType(defaultAvatar.getContentType());
		}

		else {
			TypedQuery<Document> query = messengerManager.createQuery(
					"SELECT a FROM Document a WHERE" + "a.contentHash =:" + avatar.getContentHash(), Document.class);
			Document resultDocument = query.getSingleResult();

			if (resultDocument != null) {
				person.getAvatar().setContent(resultDocument.getContent());
				person.getAvatar().setContentType(resultDocument.getContentType());
				
			} else {
				person.getAvatar().setContent(avatar.getContent());
				person.getAvatar().setContentType(avatar.getContentType());
			}
		}
		
		messengerManager.persist(person);
		//messengerManager.persist(person.getAvatar()); da CASCADE ?
		try {
			messengerManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT); //The request could not be completed due to a conflict with the current state of the resource. (409)
		} finally {
			messengerManager.getTransaction().begin();
		}

		return person.getAvatar();
	}

}