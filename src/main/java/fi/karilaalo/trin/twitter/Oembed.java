package fi.karilaalo.trin.twitter;

import org.springframework.data.annotation.Id;

public class Oembed {
	
	@Id long id;
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
