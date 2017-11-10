package de.sb.messenger.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import de.sb.messenger.persistence.*;


public class BaseEntity implements Comparable<BaseEntity>{
	@NotNull
	private final long identity; // nicht modifizierbar
	@NotNull
	private int version; // nicht modifizierbar
	@NotNull
	private long creationTimestamp; // nicht modifizierbar
	private final Set<Message> messageCaused; // ???
	
	
	public BaseEntity(){
		this.identity = 0;
		this.version=1;
		this.creationTimestamp = System.currentTimeMillis();
		this.messageCaused = Collections.emptySet();
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


	public Set<Message> getMessageCaused() { 
		return messageCaused;
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
