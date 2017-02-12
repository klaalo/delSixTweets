package fi.karilaalo.trin.twitter;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import twitter4j.OEmbedRequest;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

@Controller
public class TwitterCtrl {

	private final String TWITTER = "twitter";
	public static final String REQ_TOKEN_ATTR = "requestToken";
	public static final String T_AT_ATTR = "twitterAccessToken";
	private final String ERR_STAT_STR = "error";
	private final String OK_STAT_STR = "ok";
	private final String STAT_STR = "status";
	private final String COUNT_STR = "count";
	
	private final MongoTemplate mongoTpl;
	private final LimitService limitSer;
	
	Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	private TwitterConfiguration conf;
	
	public TwitterCtrl (MongoTemplate mongoTemplate,
			LimitService lService) {
		this.mongoTpl = mongoTemplate;
		this.limitSer = lService;
	}

	@GetMapping("/")
	public String getTwitterPage(HttpServletRequest req,
			Model model,
			@RequestParam(required=false) String oauth_verifier,
			HttpSession session) {

		if (oauth_verifier != null &&
        		!oauth_verifier.isEmpty()) {
        	Twitter twitter = getTwitter(session);
            RequestToken requestToken = (RequestToken) session.getAttribute(REQ_TOKEN_ATTR);
            if (requestToken == null) return TWITTER; 
            
            try {
    			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, oauth_verifier);
    			session.setAttribute(T_AT_ATTR, accessToken);
    		} catch (TwitterException e) {
    			e.printStackTrace();
    		}
            session.removeAttribute("requestToken");
        }
		if (session.getAttribute(T_AT_ATTR) != null) {
			Twitter twitter = getTwitter(session);
			try {
				model.addAttribute("twitterLoginName", twitter.getOAuthAccessToken().getScreenName());
				long user = twitter.getOAuthAccessToken().getUserId();
				if (user == conf.getTwitterSuper()) {
					model.addAttribute("superAdmin", true);
				}
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		}
		model.addAttribute("twitterLoginUrl", getTwitterLoginURL(req));
		return TWITTER;
	}

	private Map<String, String> getRetMap(String op,
			HttpServletRequest req,
			HttpSession session) {
		Map<String, String> retMap = new HashMap<String, String>();
		
		if (!getTwitterTokenSet(req.getSession())) {
			retMap.put(STAT_STR, ERR_STAT_STR);
			retMap.put(ERR_STAT_STR, "not authenticated");
			return retMap;
		}
		
		PolicyFactory pol = Sanitizers.FORMATTING;
		retMap.put("op", pol.sanitize(op));

		return retMap;
	}
	
	@GetMapping("/twitterJson")
	@ResponseBody
	public Map<String, String> getJson (@RequestParam String op,
			HttpServletRequest req,
			HttpSession session) {

		Map<String, String> retMap = getRetMap(op, req, session);
		Twitter twitter = getTwitter(session);
		if (retMap.containsKey(ERR_STAT_STR)) {
			return retMap;
		}
		
		long user;
		try {
			user = twitter.getOAuthAccessToken().getUserId();
		} catch (TwitterException e) {
			retMap.put(STAT_STR, ERR_STAT_STR);
			retMap.put(ERR_STAT_STR, "twitter error");
			e.printStackTrace();
			return retMap;
		}
		

		int count = 0;
		switch (op) {
			case "getOldTweets":
				retMap.put(COUNT_STR, String.valueOf(getMoreTweets(twitter)));
				retMap.put("timeLineLimitRemaining", 
						String.valueOf(limitSer.getLimit(twitter, limitSer.LIMIT_STR_TIMEL)));
				retMap.put(STAT_STR, "done");
				break;
			case "rateList":
				if (user != conf.getTwitterSuper()) {
					retMap.put(STAT_STR, ERR_STAT_STR);
					retMap.put(ERR_STAT_STR, "not allowed");
					return retMap;
				}
				if (limitSer.getLimit(twitter, limitSer.LIMIT_STR_RATEL) < 11) {
					retMap.put(STAT_STR, ERR_STAT_STR);
					retMap.put("reason", "ratelimit reached");
					return retMap;
				}
				try {
					Map<String, RateLimitStatus> rMap = twitter.getRateLimitStatus();
					for (String stat: rMap.keySet()) {
						retMap.put(stat, String.valueOf(rMap.get(stat).getRemaining()));
					}
					retMap.put(STAT_STR, OK_STAT_STR);
				} catch (TwitterException e) {
					retMap.put(STAT_STR, ERR_STAT_STR);
					e.printStackTrace();
				}
				break;
			case "delMyTweets":
				try {
					long owner = twitter.getOAuthAccessToken().getUserId();
					count += mongoTpl.remove(
							query(where("owner").is(owner)),
							Tweet.class).getN();
					if (count > 0) {
						mongoTpl.remove(query(where("owner").is(owner)), Oembed.class);
					}
					retMap.put(COUNT_STR, String.valueOf(count));
					retMap.put(STAT_STR, OK_STAT_STR);
				} catch (TwitterException e) {
					putTwitterEx(retMap);
					e.printStackTrace();
				}
				break;
			case "delTweetlessOembed":
				if (user != conf.getTwitterSuper()) {
					retMap.put(STAT_STR, ERR_STAT_STR);
					retMap.put(ERR_STAT_STR, "not allowed");
					return retMap;
				}
				List<Oembed> oList;
				try {
					oList = mongoTpl.find(
							query(where("owner").is(twitter.getOAuthAccessToken().getUserId())),
								Oembed.class);
					for (Oembed o: oList) {
						if (!mongoTpl.exists(
								query(where("id").is(o.getId())), Tweet.class)) {
							count += mongoTpl.remove(o).getN();
						}
					}
					retMap.put(STAT_STR, OK_STAT_STR);
					retMap.put(COUNT_STR, String.valueOf(count));
				} catch (TwitterException e) {
					putTwitterEx(retMap);
					e.printStackTrace();
				}
				break;
		}
		
		return retMap;
	}
	
	private void putTwitterEx (Map<String, String> retMap) {
		retMap.put(STAT_STR, ERR_STAT_STR);
		retMap.put(ERR_STAT_STR, "twitterException");
	}
	
	@PostMapping("/twitterJson")
	@ResponseBody
	public Map<String, List<String>> postJson (@RequestParam String op,
			HttpServletRequest req,
			HttpSession session) {
		
		Map<String, List<String>> retMap = new HashMap<String, List<String>>();
		
		if (!getTwitterTokenSet(session)) {
			String strs[] = { ERR_STAT_STR, "token not set" };
			retMap.put(STAT_STR,
						Arrays.asList(strs));
			return retMap;
		}

		Twitter twitter = getTwitter(session);

		long user;
		try {
			user = twitter.getOAuthAccessToken().getUserId();
		} catch (TwitterException e) {
			String strs[] = { ERR_STAT_STR, "twitter error"};
			retMap.put(STAT_STR, 
					Arrays.asList(strs));
			e.printStackTrace();
			return retMap;
		}
		
		log.debug(op);
		if (op.equals("delDisplayedTweets")) {
			String[] idPars = req.getParameterValues("tweetId[]");
			List<String> deleted = new ArrayList<String>();
			int count = 0;
			for (String idStr: idPars) {
				log.debug("deleting tweet: " + idStr);
				long id = Long.parseLong(idStr);
				int n = mongoTpl.remove(query(where("id").is(id)
						.and("owner").is(user)),
						Tweet.class).getN();
				if (n > 0) {
					count += n;
					mongoTpl.remove(query(where("id").is(id)), Oembed.class);
					try {
						twitter.destroyStatus(id);
					} catch (TwitterException e) {
						String[] strs = { "twitter exception" };
						retMap.put("error", Arrays.asList(strs));
						e.printStackTrace();
						return retMap;
					}
				}
				deleted.add(idStr);
			}
			retMap.put("deleted", deleted);
			String[] strs = { OK_STAT_STR, String.valueOf(count) };
			retMap.put(STAT_STR, Arrays.asList(strs));
		} else {
			String[] strs = { ERR_STAT_STR, "nothing to do" };
			retMap.put(STAT_STR, Arrays.asList(strs));
		}
		return retMap;
	}
	
	@SuppressWarnings("serial")
	@GetMapping("/oldestTweetList")
	@ResponseBody
	public List<Map<String, String>> getOldestTweetList (HttpSession session,
			HttpServletRequest req,
			@RequestParam(required=false) String skipStr) {
		
		if (!getTwitterTokenSet(req.getSession())) {
			return new ArrayList<Map<String, String>> () {{
				add(new HashMap<String, String> () {{
					put("error", "not authenticated");
				}});
			}};
		}
		
		Twitter twitter = getTwitter(session);
		ArrayList<Map<String, String>> retList = new ArrayList<Map<String, String>>();

		try {
			long owner = twitter.getOAuthAccessToken().getUserId();
			Query query;
			if (skipStr != null && !skipStr.isEmpty()) {
				query = query(where("owner").is(owner)
						.and("id").gt(Long.parseLong(skipStr)));
			} else {
				query = query(where("owner").is(owner));
			}
			query.with(new Sort(Sort.Direction.ASC, "created"));
			query.limit(6);				
			List<Tweet> tweetList = mongoTpl.find(
				query,
				Tweet.class);
			for(Tweet tweet: tweetList) {
				retList.add(
					new HashMap<String, String>() {{
						put("id", String.valueOf(tweet.getId()));
						put("oembed", getOembed(twitter, tweet.getId()));
						}}
					);
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		return retList;
		
	}
	
	private String getOembed (Twitter twitter, long tweetId) {
		Oembed oembed = mongoTpl.findOne(
				query(where("id").is(tweetId)),
				Oembed.class);
		if (oembed == null) {
			try {
				int limit = limitSer.getLimit(twitter, limitSer.LIMIT_STR_OEMB); 
				if (limit > 11) {
					OEmbedRequest req = new OEmbedRequest(tweetId, null);
					//req.setMaxWidth(550);
					String embed = twitter.getOEmbed(req).getHtml();
					oembed = new Oembed(tweetId, embed,
							twitter.getOAuthAccessToken().getUserId());
					limitSer.setLimit(twitter, limitSer.LIMIT_STR_OEMB, --limit);
					mongoTpl.save(oembed);
					return embed;
				} else {
					return "";
				}
			} catch (TwitterException e) {
				e.printStackTrace();
				return "";
			}
			   
		}
		return oembed.getOembed();
		
	}
	
	private Paging getPage (long owner) {
		   Paging page = new Paging();
		   page.setCount(400);
		   long oldest = getOldestTweetId(owner);
		   log.info("oldest: " + String.valueOf(oldest));
		   if (oldest > 0) {
			   page.setMaxId(oldest);
		   }
		   return page;
		}
		
	private long getOldestTweetId(long owner) {
		Tweet tweet = mongoTpl.findOne(
				query(where("owner").is(owner))
					.with(new Sort(
							Sort.Direction.ASC
							, "id")),
				Tweet.class);
		return tweet == null ? 0 : tweet.getId();
	}
	
	
	private int getMoreTweets(Twitter twitter) {
		long owner;
		int count = 0;
		try {
			owner = twitter.getOAuthAccessToken().getUserId();
			int round = 0;
			boolean exhausted = false;
			while (!exhausted && round < 10) {
				round++;
				int limit = limitSer.getLimit(twitter, limitSer.LIMIT_STR_TIMEL);
				if (limit < 11) {
					exhausted = true;
				}
				limitSer.setLimit(twitter, limitSer.LIMIT_STR_TIMEL, --limit);
				ResponseList<Status> tweets = twitter.getUserTimeline(
						owner, getPage(owner));
				count += tweets.size();
				log.info("tweetCount: " + tweets.size() + " round: " + round);
				if (tweets.size() > 2) {
					for (Status status : tweets) {
						mongoTpl.save(new Tweet(status));
					}
				} else {
					exhausted = true;
				}
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return count;
	}
	
	private String getTwitterLoginURL(HttpServletRequest req) {
		if (!getTwitterTokenSet(req.getSession())) {
	    	Twitter twitter = new TwitterFactory().getInstance();
	    	
	    	
	    	String url = "";
	    	Enumeration<?> e = req.getHeaderNames();
	    	while (e.hasMoreElements()) {
	    		String key = (String) e.nextElement();
	    		if (key.equalsIgnoreCase("X-Forwarded-Host")) {
	    			url = req.getScheme() +
	    					"://" +
	    					req.getHeader(key) + "/";
	    			break;
	    		}
	    	}
	    	if (url.isEmpty()) {
	    		url = req.getRequestURL().toString();
	    	}
	    	StringBuffer callbackURL;
	    	/*if (url.matches(".*trin\\.karilaalo\\.fi.*")) {
	    		url += "oma/";
	    	}*/
	    	callbackURL = new StringBuffer(url);
	        int index = callbackURL.lastIndexOf("/");
	        callbackURL.replace(index, callbackURL.length(), "").append("/");
	        //log.info("--- callback URL: " + callbackURL);
	        
	        try {
				RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL.toString());
				req.getSession().setAttribute(REQ_TOKEN_ATTR, requestToken);
				return requestToken.getAuthenticationURL();
			} catch (TwitterException ex) {
				ex.printStackTrace();
				return "token error";
			}
		} else {
			return "twitter.html";
		}
	}

    private Twitter getTwitter(HttpSession session) {
    	Twitter twitter = new TwitterFactory().getInstance();
    	if (getTwitterTokenSet(session)) {
    		twitter.setOAuthAccessToken(getTwitterAccess(session));
    	}
    	return twitter;
    }
    
    private boolean getTwitterTokenSet(HttpSession session) {
    	return getTwitterAccess(session) != null;
    }
    
    public AccessToken getTwitterAccess(HttpSession session) {
    	return (AccessToken) session.getAttribute(T_AT_ATTR);
    }
    

	
}
