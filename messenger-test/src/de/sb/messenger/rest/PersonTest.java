package de.sb.messenger.rest;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Assert;
import org.junit.Test;
import de.sb.messenger.persistence.*;

public class PersonTest extends EntityTest {
	Person entity;
	
	@Test
	public void testConstraints(){
		Validator validator = this.getEntityValidatorFactory().getValidator();
		entity = new Person(new Document());
		
		//Test if initial state has no errors
		Set<ConstraintViolation<Person>> constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 0);
		
		//Legal values, assert 0 Errors
		entity.setEmail("ab@c.de");
		entity.setPasswordHash(Person.passwordHash("testPassword"));
		entity.setVersion(1);
		constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 0);

		//Illegal values, assert 3 Errors
		entity.setEmail("ab@.de");
		entity.setPasswordHash(new byte[] {0, 1});
		entity.setVersion(-1);
		constraintViolations = validator.validate(entity);
		Assert.assertEquals(constraintViolations.size(), 3);
	}
	
	@Test
	public void testLifeCycle(){
		EntityManager em = this.getEntityManagerFactory().createEntityManager();
		em.getTransaction().begin();
		
		em.persist(entity);
		em.getTransaction().commit();		
		
		this.getWasteBasket().add(entity.getIdentity());
		
		em.close();
	}
	
}


