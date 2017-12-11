package fi.karilaalo;

import java.util.List;

public interface TweetRepository {
	
	List<Tweet> findFirst6ByOwnerOrderById(Long owner);
	List<Tweet> findFirst6ByOwnerAndIdGreaterThanOrderById(Long owner, Long id);
	Long  deleteByIdAndOwner(Long id, Long owner);
	Integer deleteByOwner(Long owner);
	Tweet findFirstByOwnerOrderByIdAsc(Long owner);
	Long countById(Long id);
	void save(Tweet tweet);

}
