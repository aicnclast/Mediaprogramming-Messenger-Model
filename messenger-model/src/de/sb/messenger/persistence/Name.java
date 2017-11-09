package de.sb.messenger.persistence;

public class Name {
	@Valid
	@NotNull(message = "Name cannot be empty")  //Example Message
	@Size(min=1, max=31)
	private String given; //modfizierbar
	@Valid
	@NotNull
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
