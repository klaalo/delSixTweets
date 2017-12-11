package fi.karilaalo;

import java.util.List;

public interface OembedRepository {
	
	Long deleteByOwner(Long owner);
	List<Oembed> findByOwner(Long owner);
	void delete(Long id);
	Oembed findOne(Long id);
	void save(Oembed oembed);

}
