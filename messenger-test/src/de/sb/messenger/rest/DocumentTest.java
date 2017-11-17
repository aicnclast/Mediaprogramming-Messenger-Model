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
		Set<ConstraintViolation<Document>> constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 0);

		//maximal content
		entity.setContent(new byte [16777215]);

		constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 0);
		
		//Illegal
		entity.setContent(new byte [0]);
		
		constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 1);

		//Illegal
		entity.setContent(new byte [16777216]);
		entity.setContentType("text-css");
		
		constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 2);
	}
	
	@Test
	public void testLifeCycle(){
		EntityManager em = this.getEntityManagerFactory().createEntityManager();
		em.getTransaction().begin();
		Document entity = new Document();
		em.persist(entity);
		em.getTransaction().commit();
		this.getWasteBasket().add(entity.getIdentity());		

		Assert.assertEquals(entity.getVersion(), 1);

		entity.setContentType("text/css");
		byte[] content = new byte [1];
		entity.setContent(content);
		
		em.persist(entity);
		em.getTransaction().commit();	
		
		em.refresh(entity);

		Assert.assertEquals(entity.getContent(), content);
		Assert.assertEquals(entity.getContentHash(), Document.mediaHash(content));
		Assert.assertEquals(entity.getContentType(), "text/css");
		
		em.close();
	}
	
}


