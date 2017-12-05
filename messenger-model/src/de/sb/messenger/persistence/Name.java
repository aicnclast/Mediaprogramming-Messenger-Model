package de.sb.messenger.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class Name implements Comparable <Name> {
	@NotNull
	@Column (name="givenName", nullable=true, updatable=true, insertable=true)
	@Size(min=1, max=31)
	@XmlElement(name="givenName")
	private String given; //modfizierbar
	
	@NotNull
	@Column (name="familyName", nullable=true, updatable=true, insertable=true)
	@Size(min=1, max=31)
	@XmlElement(name="familyName")
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
