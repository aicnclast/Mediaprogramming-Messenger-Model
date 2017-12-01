package de.sb.messenger.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import de.sb.messenger.persistence.*;


@Entity
@Table(name="BaseEntity", schema="messenger")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="discriminator", discriminatorType=DiscriminatorType.STRING)

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
@XmlSeeAlso  (value = {Person.class, Document.class, Message.class})

public class BaseEntity implements Comparable<BaseEntity>{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@XmlElement
	private final long identity; // nicht modifizierbar
	
	@Version //optimistic locking
	@Column(nullable=false, updatable=true, insertable=true)
	@XmlElement
	private int version; // nicht modifizierbar
	
	@Column( nullable=false, insertable=true, updatable=false)
	@XmlElement //(required=true)
	private long creationTimestamp; // nicht modifizierbar
	
	@OneToMany(mappedBy ="subject", cascade= CascadeType.REMOVE) //Feldname 
	@NotNull
	@XmlElement
	private final Set<Message> messagesCaused;
	
	
	public BaseEntity(){
		this.identity = 0;
		this.version=1;
		this.creationTimestamp = System.currentTimeMillis();
		this.messagesCaused = Collections.emptySet();
	}


	public long getIdentity() {
		return identity;
	}

	
	public int getVersion() {
		return version;
	}


	public long getCreationTimestamp() {
		return creationTimestamp;
	}


	public Set<Message> getMessagesCaused() { 
		return messagesCaused;
	}


	public void setVersion(int version) { //von der Datenbank vergeben
		this.version = version;
	}

	
	@Override 
    public int compareTo (BaseEntity o) {
		// return Long.compare(this.identity,  o.identity);
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = +1;
	    
	    if ( this.identity == o.identity ) {
	    	return EQUAL;
	    }
	    
	    if ( this.identity < o.identity ) {
	    	return BEFORE;
	    }

	    return AFTER;
	}
}
