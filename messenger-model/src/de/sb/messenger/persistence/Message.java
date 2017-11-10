package de.sb.messenger.persistence;

import java.util.ArrayList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="Message", schema="messenger")
@DiscriminatorValue(value="Message")
@PrimaryKeyJoinColumn(name="messageIdentity", referencedColumnName="identity")

public class Message extends BaseEntity {
	
@NotNull
@OneToMany( cascade=CascadeType.ALL,mappedBy="personIdentity")
private final Person author; //nicht modifizierbar

@NotNull
@JoinColumn(name="identity")
@ManyToOne(cascade=CascadeType.ALL)
private final BaseEntity subject; // Was ist subject???

@NotNull
@Column(name="body", nullable=false)
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
