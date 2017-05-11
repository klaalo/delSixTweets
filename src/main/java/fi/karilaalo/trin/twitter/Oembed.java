package fi.karilaalo.trin.twitter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class Oembed {
	
	@Id long id;
	@Column(columnDefinition = "TEXT")
	String oembed;
	long owner;
	
	public Oembed() {
		
	}
	
	public Oembed(long id, String oembed, long owner) {
		this.id = id;
		this.oembed = oembed;
		this.owner = owner;
	}
	
	public String getOembed() {
		return this.oembed;
	}
	
	public long getId() {
		return id;
	}

}
