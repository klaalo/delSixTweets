package fi.karilaalo;

import java.util.Collections;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import twitter4j.Twitter;
import twitter4j.TwitterException;

@Service
@PropertySource(value = "classpath:application.properties")
public class LimitService {
	
	public final String LIMIT_STR_OEMB = "/statuses/oembed";
	public final String LIMIT_STR_TIMEL = "/statuses/user_timeline";
	public final String LIMIT_STR_RATEL = "/application/rate_limit_status";
	private Logger log = Logger.getLogger(this.getClass());
	@Value("${my.twitter.limitMinutes}")
	private int limitMinutes;
	
	Cache limitCache;
	
	public LimitService() throws CacheException {
		CacheFactory cf = CacheManager.getInstance().getCacheFactory();
		limitCache = cf.createCache(Collections.EMPTY_MAP);
	}
	
	public int getLimit (Twitter twitter, String limitStr) {
		LimitObject l = getCacheObject(twitter, limitStr);
		log.trace("limit: " + l.getCount() + " :" + limitStr);
		return l.getCount();
	}
	
	private String getCacheKey (Twitter twitter, String limitStr) {
		try {
			return twitter.getOAuthAccessToken().getUserId() + limitStr;
		} catch (TwitterException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	@SuppressWarnings("unchecked")
	private LimitObject limitObjectFactory (Twitter twitter, String limitStr) {
		LimitObject l;
		try {
			String cachekey = getCacheKey(twitter, limitStr);
			log.debug("fetching new data: " + cachekey);
			l = new LimitObject(twitter.getRateLimitStatus().get(limitStr).getRemaining(), limitMinutes);
			limitCache.put(cachekey, l);
			return l;
		} catch (TwitterException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private LimitObject getCacheObject(Twitter twitter, String limitStr) {
		LimitObject l = (LimitObject) limitCache.get(getCacheKey(twitter, limitStr));
		if (l == null) {
			l = limitObjectFactory(twitter, limitStr);
		}
		if (!l.valid()) {
			l = limitObjectFactory(twitter, limitStr);
		}
		return l;
	}
	
	@SuppressWarnings("unchecked")
	public int setLimit (Twitter twitter, String limitStr, int value) {
		String cacheKey = getCacheKey(twitter, limitStr);
		log.trace("put: " + value + " :" + cacheKey);
		LimitObject l = getCacheObject(twitter, limitStr);
		l.putCount(value);
		limitCache.put(cacheKey, l);
		return value;
	}

	
}