package de.sb.messenger.rest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;

import java.util.Arrays;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

public class PersonServiceTest extends ServiceTest {
	
	@Test
	public void testCriteriaQueries() {
	// GET /people
		WebTarget target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("people")
				.queryParam("city", "Berlin");

		Response response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
		Person[] person = response.readEntity(Person[].class);
		Assert.assertEquals(person[0].getIdentity(), 2);
	}

	

    @Test
    public void testIdentityQueries() {
    	// GET /people/requester
    		WebTarget target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("people/requester");
        Response response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
        Person person = response.readEntity(Person.class);
        Assert.assertEquals(2, person.getIdentity());

    // GET /people/{id}
        target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("people/4");
        response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
        Person p = response.readEntity(Person.class);
        Assert.assertEquals("Elias", p.getName().getGiven());

    // GET /people/{id}/avatar
        target = newWebTarget("ines.bergmann@web.de", "ines")
        		.path("people/2/avatar");
        response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);

        byte[] img = response.readEntity(byte[].class);
        byte[] responseMediaHash = Document.mediaHash(img);
        String expectedHash =
                "[-122, 0, -8, -49, 26, 39, 51, -23, -47, 46, -23, 95, 56, -81, -36, -87, -34, -39, -21, 119, -70, 65, 104, -31, -82, 127, -10, -93, 102, 64, -45, -80]";
        String actualHash = Arrays.toString(responseMediaHash);
        Assert.assertEquals(expectedHash, actualHash);
        
        
    // Put /people	
		Person template = new Person(null);
		template.getName().setGiven("given");
		template.getName().setFamily("family");
		template.getAddress().setCity("Berlin");
		template.getAddress().setPostcode("12345");
		template.getAddress().setStreet("street");
		String email = System.currentTimeMillis() + "@test";
		template.setEmail(email);
		
		Entity<Person> entity = Entity.json(template);
        target = newWebTarget("ines.bergmann@web.de", "ines")
        		.path("people");
        Response putResponse = target.request()
        		.header("Set-password", "123456")
        		.put(entity);
		Assert.assertTrue(putResponse.getStatus() == 200);
        
		//clean up:
		target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("people")
				.queryParam("email", email);
		response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
		Person[] persons = response.readEntity(Person[].class);
		this.getWasteBasket().add(persons[0].getIdentity());
		
		
    //requester
        target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("requester");
        response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
        p = response.readEntity(Person.class);
        Assert.assertEquals("Ines", p.getName().getGiven());
    }
	

    @Test
    public void testMessageRelationQueries() {
    	//added Test-Data in the database before:
    		int messageCount = 4;
    		String firstText = "test";
	//people/{id}/messagesAuthored
    		WebTarget target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("people/2/messagesAuthored");
        Response response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
        Message[] messages = response.readEntity(Message[].class);
        Assert.assertEquals(messageCount, messages.length);
        Assert.assertEquals(firstText, messages[0].getBody());
    }
	
   

    @Test
    public void testPeopleRelationQueries() {
    	//GET people/{id}/peopleObserving
        WebTarget target = newWebTarget("ines.bergmann@web.de", "ines")
        			.path("people/2/peopleObserving");
        Response response = target.request().get();
        Assert.assertEquals(200, response.getStatus());
        Person[] peopleObserving = response.readEntity(Person[].class);
        Assert.assertEquals(6, peopleObserving.length);
        Assert.assertEquals(3, peopleObserving[0].getIdentity());

        target = newWebTarget("ines.bergmann@web.de", "ines")
        			.path("people/2/peopleObserved");
        response = target.request().get();
        Assert.assertEquals(200, response.getStatus());
        Person[] peopleObserved = response.readEntity(Person[].class);
        Assert.assertEquals(6, peopleObserved.length);
        Assert.assertEquals(3, peopleObserved[0].getIdentity());

    //PUT people/{id}/peopleObserved
        Form form = new Form();
        form.param("personReference", "2");
		form.param("personReference", "3");
        target = newWebTarget("ines.bergmann@web.de", "ines")
    			.path("people/2/peopleObserved");
        response = target.request()
        		.put(Entity.entity(form, APPLICATION_FORM_URLENCODED));
        Assert.assertEquals(204, response.getStatus());

    //check if put worked with GET
        target = newWebTarget("ines.bergmann@web.de", "ines")
        		.path("people/2/peopleObserved");
        response = target.request().get();
        Assert.assertEquals(200, response.getStatus());
        Person[] newObserved = response.readEntity(Person[].class);
        Assert.assertEquals(2, newObserved.length);
        Assert.assertEquals(2, newObserved[0].getIdentity());
        
    //reset to old value with PUT
        form = new Form();
        for(Person p: peopleObserved) {
        		form.param("personReference", String.valueOf(p.getIdentity()));
        }
        target = newWebTarget("ines.bergmann@web.de", "ines")
    			.path("people/2/peopleObserved");
        response = target.request()
        		.put(Entity.entity(form, APPLICATION_FORM_URLENCODED));
        Assert.assertEquals(204, response.getStatus());
    }

    
    @Test
    public void testLifeCycle() {
    	//PUT /people
		WebTarget target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("people");
        Person person = new Person(null);
		person.getName().setGiven("given");
		person.getName().setFamily("family");
		person.getAddress().setCity("Berlin");
		person.getAddress().setPostcode("12345");
		person.getAddress().setStreet("street");
		person.setEmail(System.currentTimeMillis() + "@test");
		
        Response response = target.request()
        		.put(Entity.entity(person, APPLICATION_JSON));
        Assert.assertEquals(204, response.getStatus());
        long id = response.readEntity(Long.class);
        this.getWasteBasket().add(id);
        
    // check if put worked with GET/{id}
        target = newWebTarget("ines.bergmann@web.de", "ines")
        			.path("people/" + id);
        response = target.request().get();
        Assert.assertEquals(200, response.getStatus());
		Person p = response.readEntity(Person.class);
        Assert.assertEquals("given", p.getName().getGiven());

    //DELETE 
        target = newWebTarget("ines.bergmann@web.de", "ines")
        		.path("entities/" + id);
        response = target.request().delete();
        Assert.assertEquals(204, response.getStatus());
                                                                                                    // EntityService
    //check if DELETE worked
        target = newWebTarget("ines.bergmann@web.de", "ines")
        			.path("people/" + id);
        response = target.request().get();
        Assert.assertEquals(404, response.getStatus()); 
        if (response.getStatus() == 404) {
            this.getWasteBasket().remove(id); 
        }
    }
    
}
