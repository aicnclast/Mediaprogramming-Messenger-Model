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

public class MessageServiceTest extends ServiceTest {
	

    @Test
    public void testIdentityQueries() {
    //PUT /messages
        Form form = new Form();
        form.param("body", "Text");
		form.param("authorReference", "2");
		form.param("subjectReference", "3");
        WebTarget target = newWebTarget("ines.bergmann@web.de", "ines")
    				.path("messages");
        Response response = target.request()
            		.put(Entity.entity(form, APPLICATION_FORM_URLENCODED));
        Assert.assertEquals(204, response.getStatus());
        long id = response.readEntity(Long.class);
        this.getWasteBasket().add(id);

    	
    	// GET /messages/id
    		target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("messages/" + id);
        Response newResponse = target.request().get();
		Assert.assertTrue(newResponse.getStatus() == 200);
        Message message = newResponse.readEntity(Message.class);
        Assert.assertEquals("Text", message.getBody());

    // GET /messages/{id}/author
        target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("messages/" + id + "/author");
        response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
        Person p = response.readEntity(Person.class);
        Assert.assertEquals(2, p.getIdentity());

    // GET /messages/{id}/subject
        target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("messages/" + id + "/subjectr");
        response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
        p = response.readEntity(Person.class);
        Assert.assertEquals(3, p.getIdentity());

    	// GET /entities/{id}/messagesCaused
    		target = newWebTarget("ines.bergmann@web.de", "ines")
				.path("entities/3/messagesCaused");
        response = target.request().get();
		Assert.assertTrue(response.getStatus() == 200);
        message = response.readEntity(Message.class);
        Assert.assertEquals(id, message.getIdentity());
        
    // DELETE 
	    target = newWebTarget("ines.bergmann@web.de", "ines")
	    		.path("entities/" + id);
	    response = target.request().delete();
	    Assert.assertEquals(204, response.getStatus());
                                                                                                    // EntityService
    //check if DELETE worked
        target = newWebTarget("ines.bergmann@web.de", "ines")
        			.path("messages/" + id);
        response = target.request().get();
        Assert.assertEquals(404, response.getStatus()); 
        if (response.getStatus() == 404) {
            this.getWasteBasket().remove(id); 
        }
    }
    
	
}
