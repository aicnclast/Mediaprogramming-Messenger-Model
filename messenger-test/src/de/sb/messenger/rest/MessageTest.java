package de.sb.messenger.rest;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Assert;
import org.junit.Test;
import de.sb.messenger.persistence.*;

public class MessageTest extends EntityTest {
	
	@Test
	public void testConstraints(){
		Validator validator = this.getEntityValidatorFactory().getValidator();
		Person person = new Person(null);
		Message entity = new Message(person, new BaseEntity());
		
		entity.setBody("test");
		Assert.assertEquals(validator.validate(entity).size(), 0);
		entity.setBody(null);
		Assert.assertEquals(validator.validate(entity).size(), 1);
		
	}
	
	@Test
	public void testLifeCycle(){
		EntityManager em = this.getEntityManagerFactory().createEntityManager();
		em.getTransaction().begin();
		Person author = em.find(Person.class, 2l);
		BaseEntity subject = em.find(Person.class, 3l);

		Message message = new Message(author, subject);
		message.setBody("test");
		em.persist(message);
		try {
			em.getTransaction().commit();
		} finally { 
			em.getTransaction().begin();
		}		
		final long id = message.getIdentity();
		this.getWasteBasket().add(id);
		em.clear();
		
		message = em.find(Message.class, id);
		Assert.assertEquals(message.getBody(), "test");
		Assert.assertEquals(message.getAuthor(), author);
		Assert.assertEquals(message.getSubject(), subject);
		
		em.close();
	}
	
}


