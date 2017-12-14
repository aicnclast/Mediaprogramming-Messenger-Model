package de.sb.messenger.persistence;

import javax.persistence.Column;
import java.lang.Comparable.*;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Embeddable
public class Name implements Comparable<Name> {
	@NotNull
	@Column (name="givenName", nullable=true, updatable=true, insertable=true)
	@Size(min=1, max=31)
	private String given; //modfizierbar
	
	@NotNull
	@Column (name="familyName", nullable=true, updatable=true, insertable=true)
	@Size(min=1, max=31)
	private String family; //modifizierbar
	
	
	public Name(){
	}
	
	public String getGiven() {
		return given;
	}
	public String getFamily() {
		return family;
	}
	
	public void setGiven(String given) {
		this.given = given;
	}
	
	public void setFamily(String family) {
		this.family = family;
	}

 @Override
	public int compareTo(Name name) {
		final int compare = this.family.compareTo(name.family);
		return compare!=0 ? compare : this.given.compareTo(name.given);
	}
	

}
