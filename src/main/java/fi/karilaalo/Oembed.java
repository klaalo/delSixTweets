package fi.karilaalo;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class Oembed {
	
	@Id long id;
	String oembed;
	@Index long owner;
	
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
