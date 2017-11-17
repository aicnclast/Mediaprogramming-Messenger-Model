package de.sb.messenger.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Embeddable
public class Name {
	@NotNull
	@Column(name="given", nullable=false)
	@Size(min=1, max=31)
	private String given; //modfizierbar
	
	@NotNull
	@Column(name="given", nullable=false)
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

}
