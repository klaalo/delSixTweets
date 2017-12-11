package fi.karilaalo;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	public static final String[] PERM_ALL = {"/", "/favicon.ico", 
			"/sign-in-with-twitter-link.png",
			"/oldestTweetList", "/twitterJson"
			, "/_ah/**"
			};
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers(PERM_ALL
					).permitAll()
			.and().authorizeRequests()
			.and().authorizeRequests()
				.anyRequest().denyAll();
			
	}
	
}
