package uk.ac.man.cs.eventlite.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

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

	private String normalisedInstanceUrl() {
		if (instanceUrl.endsWith("/")) {
			return instanceUrl.substring(0, instanceUrl.length() - 1);
		}

		return instanceUrl;
	}
}
