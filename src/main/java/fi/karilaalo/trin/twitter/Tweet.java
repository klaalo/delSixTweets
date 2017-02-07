package fi.karilaalo.trin.twitter;

import java.util.Date;

import twitter4j.Status;

import org.springframework.data.annotation.Id;

public class Tweet {
	
	@Id long id;
	Date created;
	Long owner;
	Status status;
	
	public Tweet() {
			
	}
	
	
	public Tweet(Status status) {
		this.id = status.getId();
		this.created = status.getCreatedAt();
		this.status = status;
		this.owner = status.getUser().getId();
	}


	public long getId() {
		return id;
	}


	public Date getCreated() {
		return created;
	}


	public Status getStatus() {
		return status;
	}
	
}
