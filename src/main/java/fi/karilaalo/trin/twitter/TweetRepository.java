package fi.karilaalo.trin.twitter;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface TweetRepository extends CrudRepository<Tweet, Long> {
	
	List<Tweet> findFirst6ByOwnerOrderByCreatedAsc(Long owner);
	List<Tweet> findFirst6ByOwnerAndIdGreaterThanOrderByCreatedAsc(Long owner, Long id);
	Long  deleteByIdAndOwner(Long id, Long owner);
	Integer deleteByOwner(Long owner);
	Tweet findFirstByOwnerOrderByIdAsc(Long owner);
	Long countById(Long id);

}
