package de.sb.messenger.persistence;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.*;
import javax.validation.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;



@Entity
@Table(name="Person", schema="messenger")
@DiscriminatorValue(value="Person")
@PrimaryKeyJoinColumn(name="personIdentity", referencedColumnName="identity")

public class Person extends BaseEntity {
	
	
	static private final byte[] defaultPasswordHash = passwordHash("");
	
	@Column(name="email",unique=true, nullable=false)
	@Pattern(regexp="^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
	@Size(min=1 , max=64)
	@NotNull
	private String email; //modifizierbar
	
	
	@Column(name="passwordHash",unique=true, nullable=false)
	@Size(min=32 , max=32)
	@NotNull
	private byte [] passwordHash; //modifizierbar (falls neues Passwort)
	
	
	@Column (name="group", nullable=false, insertable=false)
	@Enumerated(EnumType.STRING)
	private Group group; //nicht modifizierbar
	
	@Valid
	@Embedded
	private final Name name; //(nicht) modifizierbar
	
	@Valid
	@Embedded
	private final Address address; // (nicht) modifizierbar 
	
	@Basic(fetch=FetchType.LAZY)
	@JoinColumn(name="documentIdentity")
	private Document avatar;  //(nicht) modifizierbar
	
	@OneToMany
	@JoinTable(
			name="Message",
			joinColumns = @JoinColumn(name="messageIdentity"),
			inverseJoinColumns = @JoinColumn(name="personIdentity")
			)
	private final Set <Message> messageAuthored; //Design Pattern: Brücke
	
	@ManyToOne(fetch=FetchType.LAZY)
	//@JoinColumn(name="personIdentity")
	@JoinTable(
			name="ObservationAssociation",
			joinColumns = @JoinColumn(name="peopleObserved", referencedColumnName="personIdentity"),
			inverseJoinColumns = @JoinColumn(name="peopleObserving",referencedColumnName="personIdentity")
			)
	private final Set <Person> peopleObserved;
	
	//ObservationAssociation: Woher kommen die Werte?

	
	@OneToMany //(mappedBy="personIdentity")
	private final Set <Person> peopleObserving; //Mengenrelation: Nicht modifizierbar

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
		this.peopleObserving = Collections.emptySet();
		this.peopleObserved = new HashSet<Person>();
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
	return peopleObserved;
}

public Set<Person> getPersonObserving() {
	return peopleObserving;
}


}
