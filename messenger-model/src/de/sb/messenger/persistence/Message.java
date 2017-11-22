package de.sb.messenger.persistence;

import java.util.ArrayList;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

@Entity
@Table(name="Message", schema="messenger")
@PrimaryKeyJoinColumn(name="messageIdentity") 
public class Message extends BaseEntity {
	
//@NotNull Junit test, raus dann
@ManyToOne(fetch=FetchType.EAGER, optional = false)
@JoinColumn(name="authorReference", updatable=true, insertable=true)
private final Person author; //nicht modifizierbar

//@NotNull Junit test, raus dann
@ManyToOne(fetch=FetchType.EAGER, optional = false)
@JoinColumn(name="subjectReference", updatable=true, insertable=true)
private final BaseEntity subject; //Subject: Die Person die es erh�lt?

@NotNull
@Column(nullable=false, updatable=true, insertable=true)
@XmlElement
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

@XmlElement
public long getAuthorReference() {
	 return this.author == null ? 0 : this.author.getIdentity();
}

@XmlElement
public long getSubjectReference() {
	 return this.subject == null ? 0 : this.subject.getIdentity();
}

}
