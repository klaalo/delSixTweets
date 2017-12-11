package fi.karilaalo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:application.properties")
public class TwitterConfiguration {

	private long twitterSuper;
	
	public TwitterConfiguration (@Value("${my.twitter.twitterSuper}") String twitterSuper) {
		this.twitterSuper = Long.parseLong(twitterSuper);
	}
	
	public long getTwitterSuper() {
		return this.twitterSuper;
	}

}
