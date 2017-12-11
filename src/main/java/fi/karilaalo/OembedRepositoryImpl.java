package fi.karilaalo;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.googlecode.objectify.ObjectifyService;

@Service
public class OembedRepositoryImpl implements OembedRepository {


	@PostConstruct
	public void init() {
		ObjectifyService.register(Oembed.class);
	}
	
	@Override
	public Long deleteByOwner(Long owner) {
		return ofy().load().type(Oembed.class).filter("owner", owner).keys().list()
			.stream().peek(key -> ofy().delete().key(key).now()).count();
	}

	@Override
	public List<Oembed> findByOwner(Long owner) {
		return ofy().load().type(Oembed.class).filter("owner", owner).list();
	}

	@Override
	public void delete(Long id) {
		ofy().delete().type(Oembed.class).id(id).now();
	}

	@Override
	public Oembed findOne(Long id) {
		return ofy().load().type(Oembed.class).id(id).now();
	}

	@Override
	public void save(Oembed oembed) {
		ofy().save().entity(oembed).now();
	}

}
