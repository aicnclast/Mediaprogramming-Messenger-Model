package de.sb.messenger.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Document extends BaseEntity {
	//wie passwordhash
	static private final byte[] defaultContent = new byte [0];
	static private final byte[] defaultMediaHash = mediaHash(defaultContent);
	private byte[] contentHash; //
	private String contentType; // modifizierbar
	private byte [] content; // modifizierbar 

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


