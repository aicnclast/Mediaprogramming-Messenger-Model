package de.sb.messenger.rest;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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
		entity.getName().setGiven("given");
		entity.getName().setFamily("family");
		entity.getAddress().setCity("Berlin");
		entity.getAddress().setPostcode("12345");
		entity.getAddress().setStreet("street");
		
		Assert.assertEquals(validator.validate(entity).size(), 0);

		//Grenzwertig legal
		entity.setEmail(testString(59) + "@c.de");
		entity.getName().setGiven(testString(31));
		entity.getName().setFamily(testString(31));
		entity.getAddress().setCity(testString(63));
		entity.getAddress().setPostcode(testString(15));
		entity.getAddress().setStreet(testString(63));

		Assert.assertEquals(validator.validate(entity).size(), 0);
		
		//Grenzwertig illegal
		entity.setEmail(testString(124) + "@c.de");
		Assert.assertEquals(validator.validate(entity).size(), 1);
		entity.getName().setGiven(testString(32));
		Assert.assertEquals(validator.validate(entity).size(), 2);
		entity.getName().setFamily(testString(32));
		Assert.assertEquals(validator.validate(entity).size(), 3);
		entity.getAddress().setCity(testString(64));
		Assert.assertEquals(validator.validate(entity).size(), 4);
		entity.getAddress().setPostcode(testString(16));
		Assert.assertEquals(validator.validate(entity).size(), 5);
		entity.getAddress().setStreet(testString(64));
		Assert.assertEquals(validator.validate(entity).size(), 6);


		//illegal
		entity.setEmail("ac.de");
		Assert.assertEquals(validator.validate(entity).size(), 6);
	}
	
	@Test
	public void testLifeCycle(){
		
		EntityManager em = this.getEntityManagerFactory().createEntityManager();
		
		em.getTransaction().begin();
		
		Document avatar = em.find(Document.class, 1l);	
		Person person = new Person(avatar);
		person.getName().setGiven("given");
		person.getName().setFamily("family");
		person.getAddress().setCity("Berlin");
		person.getAddress().setPostcode("12345");
		person.getAddress().setStreet("street");
		person.setEmail(System.currentTimeMillis() + "@test");
		
		
		em.persist(person);
		try {
			em.getTransaction().commit();
		} finally { 
			em.getTransaction().begin();
		}
		
		final long id = person.getIdentity(); 
		
		this.getWasteBasket().add(id);
		
		em.clear();
		
		person = em.find(Person.class, id);
		Assert.assertEquals(person.getName().getGiven(), "given");
		Assert.assertEquals(person.getName().getFamily(), "family");
		Assert.assertEquals(person.getAddress().getCity(), "Berlin");
		Assert.assertEquals(person.getAddress().getPostcode(), "12345");
		Assert.assertEquals(person.getAddress().getStreet(), "street");
		em.clear();

		person = em.find(Person.class, id);
		person.getName().setFamily("family2");
		
		em.flush();
		
		try {
			em.getTransaction().commit();
		} finally { 
			em.getTransaction().begin();
		}
		em.clear();

		person = em.find(Person.class, id);
		Assert.assertEquals(person.getName().getFamily(), "family2");

		em.close();
		
	}
	
}


