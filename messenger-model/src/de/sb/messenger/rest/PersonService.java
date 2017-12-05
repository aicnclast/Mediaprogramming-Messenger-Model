package de.sb.messenger.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
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
import de.sb.toolbox.net.RestCredentials;
import de.sb.toolbox.net.RestJpaLifecycleProvider;

@Path("people")
public class PersonService extends EntityService{
	final EntityManager messengerManager = RestJpaLifecycleProvider.entityManager("messenger");
	


	@GET
    @Path("people")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Person[] getPersonByCriteria( 
    		@HeaderParam("Authorization") final String authentication, 
    	  	@QueryParam("familyName") String family,
    		@QueryParam("givenName") String given,
    		@QueryParam("street") String street,
    		@QueryParam("city") String city,
			@QueryParam("postcode") String postcode,
			@QueryParam("email") String email,
			@QueryParam("timestamp") String creationTimestamp) 		
 {
		
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		
		 TypedQuery<Person> query = messengerManager.createQuery
				 ("SELECT p FROM Person p WHERE"
			//	+":creationTimestamp is null OR p.creationTimeStamp BETWEEN creationTimestamp"
			    + ":email is null OR p.email =:email  AND"
				+ ":familyName is null OR p.familyName  =:familyName AND"
				+ ":givenName  is null OR p.givenName =:givenName AND"
				+ ":street  is null OR p.street =:street AND"
				+ ":city  is null OR p.city =:city AND"
				+ ":postcode is null OR p.postcode =:postcode"
				+ "ORDER BY familyName, givenName, email",
			        Person.class);
			    List<Person> result = query.getResultList();
			   return result.toArray(new Person[result.size()]);
 }	
	
	@PUT
    @Path("people")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public long updatePerson (@HeaderParam("Authorization") final String authentication, 
						 	    @Valid Person template,
						    	@HeaderParam("SetPassword") final String setPassword
    		){
		
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		
		final boolean insertMode = (template.getIdentity()==0); //if template is zero it is true
		Person person;
		
		if (insertMode) {
		 person = (new Person(null));	
		} else {
		 person = messengerManager.find(Person.class, template.getIdentity());
		}

		person.setEmail(template.getEmail());
		person.getName().setGiven(template.getName().getGiven());
		person.getName().setFamily(template.getName().getFamily());
		person.passwordHash(setPassword);
		person.getAddress().setCity(template.getAddress().getCity());
		person.getAddress().setPostcode(template.getAddress().getPostcode());
		person.getAddress().setStreet(template.getAddress().getStreet());
		person.getAvatar().setContent(template.getAvatar().getContent());
		
		messengerManager.getTransaction().begin();
		messengerManager.persist(person);
		messengerManager.getTransaction().commit();
		
		final long identity = person.getIdentity();
		return identity;
	}


	
	@GET
	@Path("people/{identity}")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person getPersonByID(@HeaderParam("Authorization") final String authentication, @PathParam("identity") final long id) {
		
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		
		Person person = messengerManager.find(Person.class, id);
		if (person == null) {
			throw new NotFoundException();
		}
		return person;
	}

	
	@GET
	@Path("people/{identity}/messagesAuthored")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Message[] getPersonMessagesAuthored(@HeaderParam("Authorization") final String authentication, 
											 @PathParam("identity") final long id) {
		
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		
		Person person = messengerManager.find(Person.class, id);
		if (person == null) {
			throw new NotFoundException();
		}
			
		final Message[] messages = person.getMessageAuthored().toArray(new Message[0]);
		Arrays.sort(messages); //comparable was implemented for BaseEntity --> id as the criteria for the natural order
		return messages;
	}
	
	
	@GET
	@Path("people/{identity}/peopleObserving")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person[] getPersonPeopleObserving(@HeaderParam("Authorization") final String authentication, 
											 @PathParam("identity") final long id) {
		
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		
		Person person = messengerManager.find(Person.class, id);
		if (person == null) {
			throw new NotFoundException();
		}
				
		Person[] people = person.getPersonObserved().toArray(new Person[0]);
		Arrays.sort(people, Comparator.comparing((Person p)->p.getName().getFamily())
		          .thenComparing(p->p.getName().getGiven())
		          .thenComparing(p->p.getEmail()));
		
		return people;
	}

	
	@GET
	@Path("people/{identity}/peopleObserved")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public Person[] getPersonPeopleObserved(@HeaderParam("Authorization") final String authentication, 
											 @PathParam("identity") final long id) {
		
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		
		Person person = messengerManager.find(Person.class, id);
		if (person == null) {
			throw new NotFoundException();
		}
		
		Person[] people = person.getPersonObserving().toArray(new Person[0]);
		Arrays.sort(people, Comparator.comparing((Person p)->p.getName().getFamily())
		          .thenComparing(p->p.getName().getGiven())
		          .thenComparing(p->p.getEmail()));
		
		return people;
		
	}
	
	/*PUT /people/{identity}/peopleObserved: Updates the person matching the
given identity to monitor the people matching the form-supplied collection of person
identities. Hint: Make sure all people whose Mirror-Relations change due to this
operation are evicted from the 2nd-level cache!*/
	
	@PUT
	@Path("people/{identity}/peopleObserved")
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public void updatePersonPeopleObserved(@HeaderParam("Authorization") final String authentication, 
											 @PathParam("identity") final long id,
											 @FormParam("peopleObserved") final Set<Long> peopleObservedIds) {
		
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
		
		Person person = messengerManager.find(Person.class, id);
		
		if (person == null) throw new NotFoundException();		
		
		for (long identity : peopleObservedIds) {
			
			if (!peopleObservedIds.contains(person.getPersonObserved().contains(messengerManager.find(Person.class, identity)))){
				person.getPersonObserved().remove(messengerManager.find(Person.class, identity));
			}
			else {
				person.getPersonObserved().add(messengerManager.find(Person.class, identity));
				
			}
		}
		 
		messengerManager.getEntityManagerFactory().getCache().evict(person.getClass(), id);
		
		  try {
			    
				messengerManager.getTransaction().commit();
				
			} catch (final RollbackException exception) {
				throw new ClientErrorException(CONFLICT);
				
			} finally {
				messengerManager.getTransaction().begin();
			}
		
	}
		
	/*Returns the avatar content of the person
matching the given identity (plus it's content type as part of the HTTP response
header). Hint: Use @Produces(WILDCARD) to declare production of an a priori unknown
media type, and return an instance of Result that contains both the document's media
type and it's content.
*/
	@GET
	@Path("people/{identity}/avatar")
	@Produces (MediaType.WILDCARD)
	public Response getPersonAvatar(@HeaderParam("Authorization") final String authentication, 
											 @PathParam("identity") final long id,
											 @HeaderParam("contentType") final String contentType) {
		
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
	
		Person person = messengerManager.find(Person.class, id);
		if (person == null) throw new NotFoundException();
				
		byte [] content = person.getAvatar().getContent();
		
		Response result;
	    if (content != null) {
	        result = Response.ok(content,contentType).build();
	      
	    } else {
	        result = Response.noContent().build();
	}
	    return result;
	}
	
	/*Updates the person's avatar (owner only), with
the document content being passed within the HTTP request body, and the media type
passed as Header-Field “Content-type”. If the given content is empty, the person's
avatar shall be set to the default document (identity=1). Otherwise, if a document
matching the media hash of the given content already exists, then this document shall
become the person's avatar. Otherwise, the given content and content-type is used to
create a new document which becomes the person's avatar. Hint: Use
@Consumes(WILDCARD) to declare consumption of an a priori unknown media type.*/
	
	@PUT
	@Path("people/{identity}/avatar")
	@Consumes (MediaType.WILDCARD)
	public Document updatePersonAvatar(@HeaderParam("Authorization") final String authentication, 
											 @PathParam("identity") final long id,
											 @HeaderParam("contentType") final String contentType,
											 @Valid Document avatar) {
		
		Authenticator.authenticate(RestCredentials.newBasicInstance(authentication));
	
		Person person = messengerManager.find(Person.class, id);
		Document defaultAvatar = messengerManager.find(Document.class, 1);
		
		if (person == null) throw new NotFoundException();
		
	    if (avatar == null) {
	       person.getAvatar().setContent(defaultAvatar.getContent()); // identity = 1 ??
	       person.getAvatar().setContentType(defaultAvatar.getContentType()); 
	       

	    } else if (messengerManager.find(Document.class, avatar.getIdentity()) != null) {
	    	person.getAvatar().setContent(messengerManager.find(Document.class, avatar.getIdentity()).getContent());
	    
	    } else {
	    	person.getAvatar().setContent(avatar.getContent());
	    	person.getAvatar().setContentType(avatar.getContentType());	    	
	    }
	     
	    avatar = person.getAvatar();
	    
	    try {
	    
	    	messengerManager.persist(avatar);
			messengerManager.getTransaction().commit();
			
		} catch (final RollbackException exception) {
			throw new ClientErrorException(CONFLICT);
			
		} finally {
			messengerManager.getTransaction().begin();
		}
	    
	    return avatar;
	}

	
	
}