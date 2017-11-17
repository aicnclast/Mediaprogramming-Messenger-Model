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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;



@Entity
@Table(name="Person", schema="messenger")
@PrimaryKeyJoinColumn(name="personIdentity")

public class Person extends BaseEntity {
	
	
	@Size(min=32, max=32)
	@XmlTransient
	static private final byte[] defaultPasswordHash = passwordHash("");
	
	@Column(name="email",unique=true, nullable=false)
	@Pattern(regexp="^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
	@Size(min=1 , max=64)
	@NotNull
	@XmlElement
	private String email; //modifizierbar
	
	
	@Column(name="passwordHash",unique=true, nullable=false)
	@Size(min=32 , max=32)
	@NotNull
	@XmlTransient
	private byte [] passwordHash; //modifizierbar (falls neues Passwort)
	
	
	@Column (name="groupAlias", nullable=false, insertable=false)
	@Enumerated(EnumType.STRING)
	
	@XmlElement
	private Group group; //nicht modifizierbar
	
	@Valid
	@Embedded
	
	@XmlElement
	private final Name name; //(nicht) modifizierbar
	
	@Valid
	@Embedded	
	@XmlElement
	private final Address address; // (nicht) modifizierbar 
	
	@Basic(fetch=FetchType.LAZY)
	@JoinColumn(name="content")	
	@XmlElement
	private Document avatar;  //(nicht) modifizierbar
	
	@OneToMany(mappedBy="author")
	@XmlElement
	private final Set <Message> messageAuthored; //Design Pattern: Br�cke
	
	
	//wie wird ein jointable dargestellt?
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(
			name="ObservationAssociation",
			joinColumns = @JoinColumn(name="peopleObserved", referencedColumnName="personIdentity"),
			inverseJoinColumns = @JoinColumn(name="peopleObserving",referencedColumnName="personIdentity")
			)
	private final Set <Person> peopleObserved;
	
	
	@ManyToMany (mappedBy="peopleObserved")
	@XmlInverseReference(mappedBy="peopleObserved")
	private final Set <Person> peopleObserving; //Mengenrelation: Nicht modifizierbar

	static public enum Group {
		ADMIN, USER;
	}
	
	@Size(min=32, max=32)
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

@XmlElement
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
