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
		WebTarget target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("people").
				queryParam("city", "Berlin");

		Response response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
		Person[] person = response.readEntity(Person[].class);

		Assert.assertEquals(person[0].getIdentity(), 2);
	}
	
	
}
