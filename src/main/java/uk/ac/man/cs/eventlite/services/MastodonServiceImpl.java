package uk.ac.man.cs.eventlite.services;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import okhttp3.OkHttpClient;

@Service
public class MastodonServiceImpl implements MastodonService {

	private final RestClient restClient;

	@Value("${mastodon.instance-url:}")
	private String instanceUrl;

	@Value("${mastodon.access-token:}")
	private String accessToken;

	@Value("${mastodon.visibility:unlisted}")
	private String visibility;

	public MastodonServiceImpl() {
		restClient = RestClient.create();
	}

	@Override
	public void publishStatus(String content) {
		if (!StringUtils.hasText(instanceUrl) || !StringUtils.hasText(accessToken)) {
			throw new IllegalStateException("Mastodon is not configured.");
		}

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("status", content);
		form.add("visibility", visibility);

		restClient.post()
				.uri(normalisedInstanceUrl() + "/api/v1/statuses")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.toBodilessEntity();
	}

	@Override
	public List<MastodonPost> getLatestPosts() {
		try {
			MastodonClient client = new MastodonClient.Builder(
					normalisedInstanceUrl().replace("https://", "").replace("http://", ""),
					new OkHttpClient.Builder(),
					new Gson()).accessToken(accessToken).build();

			Timelines timelines = new Timelines(client);
			List<Status> allPosts = new ArrayList<>();
			Range range = new Range(null, null, 40);

			for (int i = 0; i < 5; i++) {
				List<Status> page = timelines.getHome(range).execute().getPart();

				if (page.isEmpty()) {
					break;
				}

				allPosts.addAll(page);

				long lastId = page.get(page.size() - 1).getId();
				range = new Range(lastId, null, 40);
			}

			return allPosts.stream()
					.filter(post -> post.getAccount() != null && post.getAccount().getAcct() != null
							&& post.getAccount().getAcct().contains("manc_bee_f12"))
					.sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt())).limit(3)
					.map(post -> {
						ZonedDateTime ukTime = Instant.parse(post.getCreatedAt()).atZone(ZoneId.of("Europe/London"));

						MastodonPost mastodonPost = new MastodonPost();
						mastodonPost.setTime(ukTime.format(DateTimeFormatter.ofPattern("HH:mm")));
						mastodonPost.setDate(ukTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
						mastodonPost.setContent(post.getContent());
						mastodonPost.setUrl(post.getUrl());

						return mastodonPost;
					}).toList();

		} catch (Exception e) {
			e.printStackTrace();
			return List.of();
		}
	}

	private String normalisedInstanceUrl() {
		if (instanceUrl.endsWith("/")) {
			return instanceUrl.substring(0, instanceUrl.length() - 1);
		}

		return instanceUrl;
	}
}
