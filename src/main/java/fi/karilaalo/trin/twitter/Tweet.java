package fi.karilaalo.trin.twitter;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import twitter4j.Status;


@Entity
public class Tweet {
	
	@Id long id;
	Date created;
	Long owner;
	@Lob Status status;
	
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
