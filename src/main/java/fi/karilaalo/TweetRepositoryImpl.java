package fi.karilaalo;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

@Service
public class TweetRepositoryImpl implements TweetRepository {
	
	@PostConstruct
	public void init() {
		ObjectifyService.register(Tweet.class);
	}

	private void delete(Tweet entity) {
		ofy().delete().type(Tweet.class).id(entity.getId()).now();
	}


	@Override
	public List<Tweet> findFirst6ByOwnerOrderById(Long owner) {
		return ofy().load().type(Tweet.class).filter("owner", owner).orderKey(false).limit(6).list();
	}

	@Override
	public List<Tweet> findFirst6ByOwnerAndIdGreaterThanOrderById(Long owner, Long id) {
		return ofy().load().type(Tweet.class).filter("owner", owner)
				.filterKey(">", Key.create(Tweet.class, id))
				.orderKey(false).limit(6).list();
	}

	@Override
	public Long deleteByIdAndOwner(Long id, Long owner) {
		Tweet tweet = ofy().load().type(Tweet.class).id(id).now(); 
		if (tweet.owner.equals(owner)) {
			delete(tweet);
			return new Long(1);
		} else {
			return new Long(0);
		}
	}

	@Override
	public Integer deleteByOwner(Long owner) {
		return (int) ofy().load().type(Tweet.class).filter("owner", owner).list()
			.stream().peek(tweet -> delete(tweet)).count();
	}

	@Override
	public Tweet findFirstByOwnerOrderByIdAsc(Long owner) {
		return ofy().load().type(Tweet.class).filter("owner", owner).orderKey(false).first().now();
	}

	@Override
	public Long countById(Long id) {
		return (long) (ofy().load().type(Tweet.class).id(id).now() == null ? 0 : 1);
	}

	@Override
	public void save(Tweet tweet) {
		ofy().save().entity(tweet).now();
	}


}
