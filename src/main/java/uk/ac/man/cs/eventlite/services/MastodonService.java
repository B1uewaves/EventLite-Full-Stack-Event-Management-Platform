package uk.ac.man.cs.eventlite.services;

import java.util.List;

public interface MastodonService {

	void publishStatus(String content);

	List<MastodonPost> getLatestPosts();
}
