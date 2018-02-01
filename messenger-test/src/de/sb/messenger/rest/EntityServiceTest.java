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

import de.sb.messenger.persistence.BaseEntity;
import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;

import org.eclipse.persistence.jaxb.JAXBContextProperties;

public class EntityServiceTest extends ServiceTest {
		

	//move this to messageServiceTest
    @Test
    public void testIdentityQueries() {
    	// GET /entities/{identity}/messagesCaused
    		WebTarget target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("entities/3/messagesCaused");
        Response response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
        Message message = response.readEntity(Message.class);
        Assert.assertEquals(1, message.getIdentity());

    }
	
	
}
