package de.sb.messenger.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.*;

public class Person extends BaseEntity {
	static private final byte[] defaultPasswordHash = passwordHash("");
	@NotNull
	@Size(min=1, max=128)
	@Pattern(regexp = "[A-Z0-9._-]+@[A-Z0-9.-].[A-Z]")
	private String email; //modifizierbar
	@NotNull	
	@Size(min=32, max=32)
	private byte [] passwordHash; //modifizierbar (falls neues Passwort)
	@NotNull
	private Group group; //nicht modifizierbar
	@NotNull
	@Valid
	private final Name name; //(nicht) modifizierbar
	@NotNull
	@Valid
	private final Address address; // (nicht) modifizierbar 
	@NotNull
	private Document avatar;  //(nicht) modifizierbar
	private final Set <Message> messageAuthored; //Design Pattern: Brücke
	private final Set <Person> personObserved;
	private final Set <Person> personObserving; //Mengenrelation: Nicht modifizierbar
	
	static public enum Group {
		ADMIN, USER;
	}

	static public byte[] passwordHash (String password)  {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch(NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
		return md.digest(password.getBytes(StandardCharsets.UTF_8));
	}

	protected Person() {
		this(null); //Public Constructor
	}
	
	public Person (Document avatar) {  
		this.group = Group.USER;
		this.name = new Name();
		this.address = new Address();
		this.avatar = avatar;
		this.passwordHash = defaultPasswordHash;
		this.messageAuthored = Collections.emptySet();
		this.personObserving = Collections.emptySet();
		this.personObserved = new HashSet<Person>();
	}
	

public void setEmail(String email) {
		this.email = email;
	}

public void setPasswordHash(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}

public long getAvatarReference() {  
	return this.avatar == null ? 0 : this.avatar.getIdentity();
} 

public String getEmail() {
	return email;
}

public byte[] getPasswordHash() {
	return passwordHash;
}

public Group getGroup() {
	return group;
}

public Name getName() {
	return name;
}

public Address getAddress() {
	return address;
}

public Document getAvatar() {
	return avatar;
}

public Set<Message> getMessageAuthored() {
	return messageAuthored;
}

public Set<Person> getPersonObserved() {
	return personObserved;
}

public Set<Person> getPersonObserving() {
	return personObserving;
}


}
