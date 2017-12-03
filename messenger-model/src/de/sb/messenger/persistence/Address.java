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
public class Address {
	
	@Column( nullable=true, updatable=true, insertable=true)
	@Size(max=63)
	@XmlElement
	private String street; // modifizierbar
	
	@XmlElement
	@Column( nullable=true, updatable=true, insertable=true)
	@Size(max=15)
	private String postcode; // modifizierbar
	
	@XmlElement
	@NotNull
	@Column( nullable=true, updatable=true, insertable=true)
	@Size(min=1, max=63)
	private String city; //modifizierbar
	
	
	public Address(){	//package private wenn ... 
	}
	
	public void setStreet(String street) {
		this.street = street;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}
	public String getPostcode() {
		return postcode;
	}
	public String getCity() {
		return city;
	}
	

}
