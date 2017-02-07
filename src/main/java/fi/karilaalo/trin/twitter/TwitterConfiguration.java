package fi.karilaalo.trin.twitter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwitterConfiguration {

	private long twitterSuper;
	
	public TwitterConfiguration (@Value("${my.twitter.twitterSuper}") String twitterSuper) {
		this.twitterSuper = Long.parseLong(twitterSuper);
	}
	
	public long getTwitterSuper() {
		return this.twitterSuper;
	}

}
