package de.sb.messenger.persistence;

public class Address {
	private String street; // modifizierbar
	private String postcode; // modifizierbar
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
