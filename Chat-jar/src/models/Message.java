package models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private User receiver;
	private User sender;
	private LocalDateTime dateCreated;
	private String subject;
	private String content;
	
	public Message() { }

	public Message(User receiver, User sender, LocalDateTime dateCreated, String subject, String content) {
		super();
		this.receiver = receiver;
		this.sender = sender;
		this.dateCreated = dateCreated;
		this.subject = subject;
		this.content = content;
	}

	public User getReceiver() {
		return receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public LocalDateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(LocalDateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
