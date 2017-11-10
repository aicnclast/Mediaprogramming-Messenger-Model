package de.sb.messenger.persistence;

import java.util.ArrayList;

public class Message extends BaseEntity {
@NotNull
private final Person author; //nicht modifizierbar
@NotNull
private final BaseEntity subject; // nicht modifizierbar
@NotNull
@Size(min=1, max=4093)
private String body; //modifierbar

protected Message(){ //zur Initialisierung
	this(null,null);
}

public Message(Person author, BaseEntity subject ){ //zur Initialisierung
	this.author = author;
	this.subject = subject;
	this.body = "";
}

public Person getAuthor() {
	return author;
}

public BaseEntity getSubject() {
	return subject;
}

public String getBody() {
	return body;
}

public void setBody(String body) {
	this.body=body;
}

public long getAuthorReference() {
	 return this.author == null ? 0 : this.author.getIdentity();
}

}
