package de.sb.messenger.persistence;
import javax.validation.Valid;
import javax.validation.constraints.*;

public class Address {
	@Size(min=0, max=63)
	@Valid
	private String street; // modifizierbar
	
	
	@Valid
	@Size(min=0, max=15)
	@Pattern(regexp = "[0-9]")
	private String postcode; // modifizierbar
	
	@NotNull
	@Size(min=1, max=63)
	@Valid
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
