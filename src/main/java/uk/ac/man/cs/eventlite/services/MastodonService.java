package uk.ac.man.cs.eventlite.services;

import java.util.List;
import com.sys1yagi.mastodon4j.api.entity.Status;

public interface MastodonService {

    void publishStatus(String content);

    List<MastodonPost> getLatestPosts();
}