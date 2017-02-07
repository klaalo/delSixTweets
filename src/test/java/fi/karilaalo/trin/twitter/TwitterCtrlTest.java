package fi.karilaalo.trin.twitter;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.containsString;

import org.apache.commons.lang3.RandomStringUtils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations={"classpath:application.properties"})
public class TwitterCtrlTest {

	private MockMvc mvc;
	
	@Autowired
	TwitterCtrl ctrl;
	
	
	@Autowired
	private WebApplicationContext ctx;
	
	private String hostUrl;
	private String twitterUrl;

	@LocalServerPort
    private int port;
	
	@Value("${my.twitter.accessToken}")
	private String at;
	@Value("${my.twitter.accessTokenSecret}")
	private String as;
	
	@Before
	public void init() {
		this.hostUrl =
				"http://localhost:" + port;
		this.twitterUrl = hostUrl +
				"/";
		mvc = MockMvcBuilders
				.webAppContextSetup(ctx)
				.apply(springSecurity())
				.build();
	}
	
	@Test
	public void acontextLoadsTest() {
		Assert.assertNotNull(ctrl);;
	}
	
	@Test
	public void getTwitterPageTest() throws Exception {
		mvc.perform(
				get(twitterUrl)
				)
		.andExpect(status().isOk());
		
		final String str = RandomStringUtils.randomAlphabetic(14);
		
		Twitter twitter = new TwitterFactory().getInstance();
	    final String url = twitterUrl + "?oauth_verifier=" + str;
		RequestToken rt = twitter.getOAuthRequestToken();
		mvc.perform(
				get(url).sessionAttr(TwitterCtrl.REQ_TOKEN_ATTR, rt)
				)
				.andExpect(status().isOk());
		
		AccessToken tat = new AccessToken(at, as);
		mvc.perform(get(twitterUrl).sessionAttr(TwitterCtrl.T_AT_ATTR, tat))
			.andExpect(status().isOk());
	}
	
	@Test
	public void getOldTweetListTest() throws Exception {
		
		AccessToken tat = new AccessToken(at, as);
		mvc.perform(get(hostUrl + "/oldestTweetList")
				.sessionAttr(TwitterCtrl.T_AT_ATTR, tat))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			;

		mvc.perform(get(hostUrl + "/oldestTweetList?skipStr=538023810021474304")
				.sessionAttr(TwitterCtrl.T_AT_ATTR, tat))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			;

		/*mvc.perform(get(hostUrl + "/oldestTweetList"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			;*/
		
	}
	
	@Test
	public void getJsonTest() throws Exception {

		mvc.perform(get(hostUrl + "/twitterJson?op=nonsense"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
		;
		
		AccessToken tat = new AccessToken(at, as);
		String[] ops = {"rateList", "delTweetlessOembed", "delMyTweets", "getOldTweets"};
		for (String op: ops) {
			mvc.perform(get(hostUrl + "/twitterJson?op=" + op)
					.sessionAttr(TwitterCtrl.T_AT_ATTR, tat))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			;
		}

		tat = new AccessToken(at, "nonsense");
		mvc.perform(get(hostUrl + "/twitterJson?op=rateList")
				.sessionAttr(TwitterCtrl.T_AT_ATTR, tat))
		.andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
		;

	}
	
	@Test
	public void postJsonTest() throws Exception {

		mvc.perform(post(hostUrl + "/twitterJson?op=nonsense")
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
		;

		mvc.perform(post(hostUrl + "/twitterJson")
				.with(csrf()))
			.andExpect(status().is4xxClientError())
		;
	
		mvc.perform(post(hostUrl + "/twitterJson?op=nonsense"))
			.andExpect(status().is4xxClientError())
		;
		
		AccessToken tat = new AccessToken(at, as);
		mvc.perform(post(hostUrl + "/twitterJson?op=nonsense")
				.with(csrf())
				.sessionAttr(TwitterCtrl.T_AT_ATTR, tat))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(content().string(containsString("nothing to do")))
		;
		
		String[] tweets = {"538365655100444673", "538365655100444674", "538365655100444675"};

		mvc.perform(post(hostUrl + "/twitterJson?op=delDisplayedTweets")
				.with(csrf())
				.sessionAttr(TwitterCtrl.T_AT_ATTR, tat)
				.param("tweetId[]", tweets))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
		;
	}
	
}
