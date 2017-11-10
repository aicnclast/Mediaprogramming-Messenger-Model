package de.sb.messenger.persistence;

public class Name {
	private String given; //modfizierbar
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
