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
		Person person = new Person(new Document());
		person.setEmail("ab@c.de");
		person.setPasswordHash(Person.passwordHash("testPassword"));
		person.getName().setGiven("given");
		person.getName().setFamily("family");
		person.getAddress().setCity("Berlin");
		person.getAddress().setPostcode("12345");
		person.getAddress().setStreet("street");
		
		Message entity = new Message(person, new BaseEntity());
		entity.setBody("test");
		
		Set<ConstraintViolation<Message>> constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 0);

		entity.setBody(null);

		constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 1);
		
	}
	
	@Test
	public void testLifeCycle(){
		EntityManager em = this.getEntityManagerFactory().createEntityManager();
		em.getTransaction().begin();
		Document avatar = new Document();
		em.persist(avatar);
		em.getTransaction().commit();
		this.getWasteBasket().add(avatar.getIdentity());		

		em.getTransaction().begin();
		Person person = new Person(avatar);
		em.persist(person);
		em.getTransaction().commit();	
		this.getWasteBasket().add(person.getIdentity());
		
		em.getTransaction().begin();
		BaseEntity base = new BaseEntity();
		em.persist(base);
		em.getTransaction().commit();	
		this.getWasteBasket().add(base.getIdentity());

		em.getTransaction().begin();
		Message entity = new Message(person, base);
		entity.setBody("test");
		em.persist(entity);
		em.getTransaction().commit();		
		this.getWasteBasket().add(entity.getIdentity());
		em.refresh(entity);
		
		Assert.assertEquals(entity.getBody(), "test");
		
		em.close();
	}
	
}


