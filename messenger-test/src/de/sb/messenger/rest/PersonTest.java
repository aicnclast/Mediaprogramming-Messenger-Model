package de.sb.messenger.rest;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Assert;
import org.junit.Test;
import de.sb.messenger.persistence.*;

public class PersonTest extends EntityTest {
	
	
	@Test
	public void testConstraints(){
		Validator validator = this.getEntityValidatorFactory().getValidator();
		Person entity = new Person(new Document());
		
		//Legal values, assert 0 Errors
		entity.setEmail("ab@c.de");
		entity.setPasswordHash(Person.passwordHash("testPassword"));
		entity.setVersion(1);
		entity.getName().setGiven("123");
		entity.getName().setFamily("123");
		entity.getAddress().setCity("Berlin");
		entity.getAddress().setPostcode("12345");
		entity.getAddress().setStreet("ABC");
		
		Set<ConstraintViolation<Person>> constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 0);

		//Grenzwertig legal
		entity.setEmail(testString(59) + "@c.de");
		entity.getName().setGiven(testString(31));
		entity.getName().setFamily(testString(31));
		entity.getAddress().setCity(testString(63));
		entity.getAddress().setPostcode(testString(15));
		entity.getAddress().setStreet(testString(63));

		constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 0);
		
		
		//Grenzwertig illegal
		entity.setEmail(testString(60) + "@c.de");
		entity.getName().setGiven(testString(32));
		entity.getName().setFamily(testString(32));
		entity.getAddress().setCity(testString(64));
		entity.getAddress().setPostcode(testString(16));
		entity.getAddress().setStreet(testString(64));
		
		constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 6);
		
		
		//illegal
		entity.setEmail("ac.de");
		
		constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 6);
	}
	
	@Test
	public void testLifeCycle(){
		/*EntityManager em = this.getEntityManagerFactory().createEntityManager();
		em.getTransaction().begin();
		Person entity = new Person(null);
		em.persist(entity);
		em.getTransaction().commit();		
		
		this.getWasteBasket().add(entity.getIdentity());
		
		em.close();*/
	}
	
}


