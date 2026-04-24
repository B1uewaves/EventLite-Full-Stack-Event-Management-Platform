package uk.ac.man.cs.eventlite.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.method.Timelines;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.Range;


import okhttp3.OkHttpClient;

@Service
public class MastodonService {

    private static final String BASE_URL = "https://defcon.social";
    private static final String ACCESS_TOKEN = "pN-DkFMqjAU30ybFKQLvIrO5YH5WLHB7sq6LXgkj244";
    
    public List<Status> getLatestPosts() {
        try {
            MastodonClient client = new MastodonClient.Builder(
                    "defcon.social",
                    new OkHttpClient.Builder(),
                    new com.google.gson.Gson()
            ) .accessToken(ACCESS_TOKEN) .build();

            Timelines timelines = new Timelines(client);
            Range range = new Range(null, null, 40);
            List<Status> all_posts = new ArrayList<>();
            
            for (int i = 0; i < 10; i++) { // fetch 5 pages (~100 posts)
                List<Status> page = timelines.getHome(range)
                        .execute()
                        .getPart();

                if (page.isEmpty()) break;

                all_posts.addAll(page);

                // move to older posts
                range = new Range(
                	    page.get(page.size() - 1).getId(), null, 40);  // maxIdnull, sinceId, limit
            }

//            List<Status> posts = timelines.getHome(range)
//                    .execute()
//                    .getPart();
            
            return all_posts.stream()
                    .filter(p -> p.getAccount().getAcct().equals("manc_bee_f12")) // returns posts by manc_bee_f12 only
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // newest first
                    .limit(3)  // last 3 posts
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}