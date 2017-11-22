package de.sb.messenger.rest;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Assert;
import org.junit.Test;
import de.sb.messenger.persistence.*;

public class DocumentTest extends EntityTest {
	
	@Test
	public void testConstraints(){
		Validator validator = this.getEntityValidatorFactory().getValidator();
		Document entity = new Document();
		
		//Legal values, minimal content
		entity.setContentType("text/css");
		entity.setContent(new byte [1]);
		Assert.assertEquals(validator.validate(entity).size(), 0);

		//maximal content
		entity.setContent(new byte [16777215]);
		Assert.assertEquals(validator.validate(entity).size(), 0);
		
		//Illegal
		entity.setContentType("text-css");
		Assert.assertEquals(validator.validate(entity).size(), 1);
		entity.setContent(new byte [0]);
		Assert.assertEquals(validator.validate(entity).size(), 2);
		entity.setContent(new byte [16777216]);
		Assert.assertEquals(validator.validate(entity).size(), 2);
	}
	
	@Test
	public void testLifeCycle(){
		EntityManager em = this.getEntityManagerFactory().createEntityManager();
		em.getTransaction().begin();
		Document document = new Document();
		byte[] content = new byte [1];
		document.setContent(content);
		document.setContentType("text/css");
		em.persist(document);
		try {
			em.getTransaction().commit();
		} finally { 
			em.getTransaction().begin();
		}
		final long id = document.getIdentity(); 
		this.getWasteBasket().add(id);		
		em.clear();

		document = em.find(Document.class, id);
		Assert.assertEquals(document.getContent(), content);
		Assert.assertEquals(document.getContentType(), "text/css");
		em.clear();
		
		document = em.find(Document.class, id);
		document.setContentType("text/html");
		em.flush();
		try {
			em.getTransaction().commit();
		} finally { 
			em.getTransaction().begin();
		}
		em.clear();

		document = em.find(Document.class, id);
		Assert.assertEquals(document.getContentType(), "text/html");
		
		em.close();
	}
	
}


