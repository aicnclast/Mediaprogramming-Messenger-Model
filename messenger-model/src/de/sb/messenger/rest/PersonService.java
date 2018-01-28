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
import javax.ws.rs.NotAuthorizedException;
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



@Path("people")
@Copyright(year=2017, holders="Team 4")

public class PersonService extends ServiceTest {

	
	private void  checkAccessRights(final long id, final String authentication) {
		if (id==0) {
			Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
			final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		}
		
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final Person requester = Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final Person person = messengerManager.find(Person.class, id);
		if(id != requester.getIdentity()) throw new NotAuthorizedException("Basic");
		if (person == null) throw new NotFoundException(); 
	}

	// http://localhost:8001/services/people?familyName=Bergmann&givenName=Ines&...

	@GET
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person[] getPersonByCriteria(
			@HeaderParam("Authorization") final String authentication,
			@Size(max=31) @QueryParam("familyName") final String family,
			@Size(max=31) @QueryParam("givenName") final String given,
			@Size(max=63) @QueryParam("street") final String street,
			@Size(max=63) @QueryParam("city") final String city,
			@Size(max=15) @QueryParam("postcode") final String postcode,
			@Pattern(regexp="^.+@.+$") @QueryParam("email") final String email,
			@QueryParam("lowerCreationTimestamp") long lowerCreationTimestamp,
			@QueryParam("upperCreationTimestamp") long upperCreationTimestamp,
			@QueryParam("resultOffset") int resultOffset,
			@QueryParam("resultLength") int resultLength
	) {
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
	    Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		
		String sql = "SELECT p.identity FROM Person p WHERE"
				+ " (:lowerCreationTimestamp = 0 OR p.creationTimestamp >= :lowerCreationTimestamp) AND"
				+ " (:upperCreationTimestamp = 0 OR p.creationTimestamp <= :upperCreationTimestamp) AND"
				+ " (:email is null OR p.email = :email) AND" 
				+ " (:familyName is null OR p.name.family = :familyName) AND"
				+ " (:givenName is null OR p.name.given = :givenName) AND" 
				+ " (:street is null OR p.address.street = :street) AND"
				+ " (:city is null OR p.address.city = :city) AND"
				+ " (:postcode is null OR p.address.postcode = :postcode)";
		
		Query query = messengerManager.createQuery(sql, Long.class);
		
		if(resultOffset > 0) query.setFirstResult(resultOffset);
		if(resultLength > 0) query.setMaxResults(resultLength);
		
		query.setParameter("familyName", family);
		query.setParameter("givenName", given);
		query.setParameter("street", street);
		query.setParameter("city", city);
		query.setParameter("postcode", postcode);
		query.setParameter("email", email);
		query.setParameter("lowerCreationTimestamp", lowerCreationTimestamp);
		query.setParameter("upperCreationTimestamp", upperCreationTimestamp);
	
		List<Long> result = query.getResultList();
		
        Person[] people = null;
        ArrayList<Person> peopleList = new ArrayList<Person>();
        for (int i = 0; i < result.size(); i++) {
            final Person persistedPerson = messengerManager.find(Person.class, result.get(i));

            if (persistedPerson != null) {
                peopleList.add(persistedPerson);
            }
        }

        people = peopleList.toArray(new Person[0]);
        Arrays.sort(people);

        return people;
	}

	@PUT
	@Produces((MediaType.TEXT_PLAIN))
	@Consumes ({ APPLICATION_JSON, APPLICATION_XML })
	public long updatePerson(
			@HeaderParam("Authorization") final String authentication,
			@NotNull @Valid Person template,
			@Size(min=4)@NotNull @HeaderParam("Set-password") final String setPassword
		) {		
		final Person requester = Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		if (requester.getIdentity() != template.getIdentity() && requester.getGroup() != ADMIN) 
			throw new ClientErrorException(FORBIDDEN);

		final boolean insertMode = (template.getIdentity() == 0); // if template is zero it is true
		final Person person;

		if (insertMode) {
			Document avatar = messengerManager.find(Document.class, 1l);
			if(avatar == null)
				throw new ClientErrorException(NOT_FOUND);
			person = new Person(avatar);
		} else {
			person = messengerManager.find(Person.class, template.getIdentity());
			if (person == null) throw new ClientErrorException(404);
		}

		person.setEmail(template.getEmail());
		person.getName().setGiven(template.getName().getGiven());
		person.getName().setFamily(template.getName().getFamily());
		person.setPasswordHash(Person.passwordHash(setPassword));// static methods are bound by class, not object
		person.getAddress().setCity(template.getAddress().getCity());
		person.getAddress().setPostcode(template.getAddress().getPostcode());
		person.getAddress().setStreet(template.getAddress().getStreet());

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
	
	
	// http://localhost:8001/services/requester
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
	public Person getPersonByID( @HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id) {
		//final EntityManagerFactory em = Persistence.createEntityManagerFactory("messenger");
		EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
	    Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final Person person = messengerManager.find(Person.class, id);
		if (person == null)	throw new NotFoundException();
		return person;
	}

	// http://localhost:8001/services/people/3/messagesAuthored
	@GET
	@Path("{identity}/messagesAuthored")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Message[] getPersonMessagesAuthored(
			@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id
		){

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
	@Path("{identity}/peopleObserving")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person[] getPersonPeopleObserving(@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id) {

		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final Person person = messengerManager.find(Person.class, id);
		if (person == null)
			throw new NotFoundException();

		Person[] people = person.getPersonObserved().toArray(new Person[0]);
		Arrays.sort(people, Comparator.comparing(Person::getName).thenComparing(Person::getEmail));

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
		Arrays.sort(people, Comparator
				.comparing((Person p) -> p.getName().getFamily())
				.thenComparing(p -> p.getName().getGiven())
				.thenComparing(p -> p.getEmail())
		);

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
	public void updatePersonPeopleObserved(
			@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id, 
			@NotNull @FormParam("peopleObserved") final Set<Long> peopleObservedIds
		) {

		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final Person person = messengerManager.find(Person.class, id);

		//if(id != requester.getIdentity()) throw new NotAuthorizedException("Basic");
		if (person == null)	throw new NotFoundException();

		Map<Long, Long> map = new HashMap<Long, Long>(person.getPersonObserved().size());

		for (Person p : person.getPersonObserved()) {
			map.put(p.getIdentity(), p.getIdentity());
		}

		Person wanted;
		List<Long> changed = new ArrayList<>();
		

		for (long identity : peopleObservedIds) {
			wanted = messengerManager.find(Person.class, identity);
			if (wanted == null) throw new NotFoundException();

			if (!(peopleObservedIds.contains((map.get(identity))))) {
				changed.add(wanted.getIdentity());
				person.getPersonObserved().remove(wanted);
				//messengerManager.getEntityManagerFactory().getCache().evict(Person.class, identity);
			}

			else if (!(person.getPersonObserved().contains(wanted))) {
				changed.add(wanted.getIdentity());
				person.getPersonObserved().add(wanted);
				//messengerManager.getEntityManagerFactory().getCache().evict(Person.class, identity);
			}
		}

		messengerManager.persist(person);
		try {
			messengerManager.getTransaction().commit();
			messengerManager.getEntityManagerFactory().getCache().evict(Person.class, id);
			
			for(long identity : changed) {
				messengerManager.getEntityManagerFactory().getCache().evict(Person.class, identity);
			}
		
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
	public Response getPersonAvatar(
			@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id
		) {

		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final Person person = messengerManager.find(Person.class, id);
		
		
		
		if (person == null)	throw new NotFoundException();

		byte[] content = person.getAvatar().getContent();
		String contentType = person.getAvatar().getContentType();

		return content == null ? Response.noContent().build() : Response.ok(content).header("contentType", contentType).build();
		
//		
//		Response result;
//		if (content != null) {
//			result = Response.ok(content).header("contentType", contentType).build();
//
//		} else {
//			result = Response.noContent().build();
//		}
//		return result;
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
	@Produces(MediaType.TEXT_PLAIN)
	public long updatePersonAvatar(
			@HeaderParam("Authorization") final String authentication,
			@PathParam("identity") final long id,
			@NotNull @Pattern(regexp="^[a-z]+\\/[a-z\\.\\+\\-]+$")
			@HeaderParam("Content-type") final String avatarContentType,
			byte[] avatarContent
	) {
		
		final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
		final Person requester = Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		final Person person = messengerManager.find(Person.class, id);
		if(id != requester.getIdentity()) throw new NotAuthorizedException("Basic");
		if (person == null) throw new NotFoundException(); 
		
		final Document avatar;
		if (avatarContent.length == 0) {
			avatar = messengerManager.find(Document.class, 1l);
		} else {
			TypedQuery<Document> query = messengerManager.createQuery("SELECT a FROM Document a WHERE a.contentHash = :contentHash", Document.class);
			query.setParameter("contentHash", Document.mediaHash(avatarContent));
			List <Document> resultDocuments = query.getResultList(); //More than one person can have the same avatar

			if (resultDocuments.size() == 0) {
				avatar = new Document();
				avatar.setContentType(avatarContentType);
				avatar.setContent(avatarContent);
				messengerManager.persist(avatar);
				try {
					messengerManager.getTransaction().commit();
				} finally {
					messengerManager.getTransaction().begin();	
				}
			} else {
			    avatar = resultDocuments.get(0);
			}
		}
		person.setAvatar(avatar);
		
		try {
			messengerManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT); // The request could not be completed due to a conflict 
		} finally {
			messengerManager.getTransaction().begin();	
		}

		return person.getAvatar().getIdentity();
	}

}