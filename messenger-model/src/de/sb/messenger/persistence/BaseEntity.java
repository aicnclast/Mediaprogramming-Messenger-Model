package de.sb.messenger.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import de.sb.messenger.persistence.*;


@Entity
@Table(name="BaseEntity", schema="messenger")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="discriminator", discriminatorType=DiscriminatorType.STRING)


public class BaseEntity implements Comparable<BaseEntity>{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@NotNull
	private final long identity; // nicht modifizierbar
	
	@Version
	@Column(name="version", nullable=false)
	private int version; // nicht modifizierbar
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationTimestamp", nullable=false, insertable=false, updatable=false)
	private long creationTimestamp; // nicht modifizierbar
	
	@OneToMany(mappedBy ="subject") //Feldname 
	@Column(nullable=true)
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
