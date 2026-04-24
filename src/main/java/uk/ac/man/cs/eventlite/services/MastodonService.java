package uk.ac.man.cs.eventlite.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.method.Timelines;
import com.sys1yagi.mastodon4j.api.entity.Status;

import okhttp3.OkHttpClient;

@Service
public class MastodonService {

    private static final String BASE_URL = "https://defcon.social";
    private static final String ACCESS_TOKEN = System.getenv("MASTODON_TOKEN");

    public List<Status> getLatestPosts() {
        try {
            MastodonClient client = new MastodonClient.Builder(
                    BASE_URL,
                    new OkHttpClient.Builder(),
                    new com.google.gson.Gson()
            ).accessToken(ACCESS_TOKEN).build();

            Timelines timelines = new Timelines(client);

            
            List<Status> posts = timelines.getHome()
                    .execute()
                    .getPart();

            return posts.subList(0, Math.min(3, posts.size()));

        } catch (Exception e) {
            return List.of();
        }
    }
}