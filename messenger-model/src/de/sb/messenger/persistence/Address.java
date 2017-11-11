package de.sb.messenger.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Embeddable
public class Address {
	
	@Column(name="street", nullable=true)
	@Size(min=0, max=63)
	private String street; // modifizierbar
	
	@Column(name="postcode", nullable=true)
	@Size(min=0, max=15)
	private String postcode; // modifizierbar
	
	@NotNull
	@Column(name="city", nullable=false)
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
