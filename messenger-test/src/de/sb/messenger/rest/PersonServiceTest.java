package de.sb.messenger.rest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.Arrays;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Person;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

public class PersonServiceTest extends ServiceTest {
	
	@Test
	public void testCriteriaQueries() {
	// GET /people
		WebTarget target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("people").
				queryParam("city", "Berlin");

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
		template.setEmail(System.currentTimeMillis() + "@test");
		
/*		Entity<Person> entity = Entity.json(template);
        target = newWebTarget("ines.bergmann@web.de", "ines")
        		.path("people/");
        Response putResponse = target.request()
        		.header("Set-password", "123456")
        		.put(entity);
		Assert.assertTrue(putResponse.getStatus() == 200);
        
*/		
		
    }
	
	
}
