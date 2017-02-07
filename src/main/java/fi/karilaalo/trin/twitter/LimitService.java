package fi.karilaalo.trin.twitter;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import twitter4j.Twitter;
import twitter4j.TwitterException;

@Service
@EnableCaching
public class LimitService {
	
	public final String LIMIT_STR_OEMB = "/statuses/oembed";
	public final String LIMIT_STR_TIMEL = "/statuses/user_timeline";
	public final String LIMIT_STR_RATEL = "/application/rate_limit_status";

	@Cacheable(cacheNames="twitterLimits",
		key="T(String).valueOf(#twitter.getOAuthAccessToken().getUserId()) + #limitStr")
	public int getLimit (Twitter twitter, String limitStr) {
		int ret;
		try {
			ret = twitter.getRateLimitStatus().get(limitStr).getRemaining();
		} catch (TwitterException e) {
			ret = 0;
			e.printStackTrace();
		}
		return ret;
	}
	
	@CachePut(cacheNames="twitterLimits",
			key="T(String).valueOf(#twitter.getOAuthAccessToken().getUserId()) + #limitStr")
	public int setLimit (Twitter twitter, String limitStr, int value) {
		return value;
	}
	
}
