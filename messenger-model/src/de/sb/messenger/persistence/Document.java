package de.sb.messenger.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Entity
@Table(name="Document", schema="messenger")
@PrimaryKeyJoinColumn(name="documentIdentity")
public class Document extends BaseEntity {
	//wie passwordhash
	static private final byte[] defaultContent = new byte [0];
	static private final byte[] defaultMediaHash = mediaHash(defaultContent);
	
	@NotNull
	@Column(unique=true, nullable=false, updatable=false, insertable=true)
	@Size(min=32, max=32)
	private byte[] contentHash; //
	
	@NotNull
	@Column( nullable=false, updatable=false)
	@Size(min=1, max=63)
	@Pattern(regexp="^[a-z]+\\/[a-z\\.\\+\\-]+$")
	private String contentType; // modifizierbar
	
	@NotNull
	@Column( nullable=false)
	@Size(min=1, max=16777215)
	private byte [] content; // modifizierbar 

	@Size(min=32, max=32)
	static public byte[] mediaHash (byte [] content)  {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch(NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}

		return md.digest(content);
	}
	
	
	public Document() {
		this.setContentType("text/text"); //default f�r allgemeine byte-Arrays
		this.content = defaultContent; //annotations 
		this.contentHash = defaultMediaHash;
			
	}

	public byte[] getContentHash() {
		return contentHash;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getContent() {
		return content;
	}
		
	public void setContent(byte[] content) {
		this.content = content;
		this.contentHash = mediaHash(content);
		
	}
	
	
	}


