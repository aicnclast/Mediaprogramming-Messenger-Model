package de.sb.messenger.rest;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Person;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

public class PersonServiceTest extends ServiceTest {
	
	@Test
	public void testCriteriaQueries() {
		WebTarget target = ServiceTest.newWebTarget("ines.bergmann@web.de", "ines");
		Builder request = target.path("/people")
				.queryParam("given", "Ines")
				.request();
		//JSON
		Response response = request.accept(APPLICATION_JSON)
				.get();
		Assert.assertTrue(response.getStatus() == 200);
		Person person = response.readEntity(Person.class);
		Assert.assertEquals(person.getIdentity(), 2);
		
		//XML
		response = request.accept(APPLICATION_XML)
				.get();
		Assert.assertTrue(response.getStatus() == 200);
		person = response.readEntity(Person.class);
		Assert.assertEquals(person.getIdentity(), 2);
	}
	
	
	
}
