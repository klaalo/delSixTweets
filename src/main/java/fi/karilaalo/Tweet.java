package fi.karilaalo;

import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import twitter4j.Status;


@Entity
public class Tweet {
	
	@Id long id;
	Date created;
	@Index Long owner;
	
	public Tweet() {
			
	}
	
	
	public Tweet(Status status) {
		this.id = status.getId();
		this.created = status.getCreatedAt();
		this.owner = status.getUser().getId();
	}


	public long getId() {
		return id;
	}


	public Date getCreated() {
		return created;
	}


}
