package fi.karilaalo.trin.twitter;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface OembedRepository extends CrudRepository<Oembed, Long> {
	
	Long deleteByOwner(Long owner);
	List<Oembed> findByOwner(Long owner);

}
