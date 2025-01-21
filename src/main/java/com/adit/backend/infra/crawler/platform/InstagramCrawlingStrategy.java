package com.adit.backend.infra.crawler.platform;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.infra.crawler.common.AbstractWebCrawlingStrategy;
import com.adit.backend.infra.crawler.exception.CrawlingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class InstagramCrawlingStrategy extends AbstractWebCrawlingStrategy {

	private final RestTemplate restTemplate;
	private static final String INSTAGRAM_URL = "instagram.com";

	//API 요청을 위한 토큰
	@Value("${apify.token}")
	private String apifyToken;
	//API 응답 대기 시간 간격
	@Value("${apify.poll-interval}")
	private long pollInterval;
	//Apify 작업 실행 API를 위한 기본 URL
	@Value("${apify.base-url}")
	private String baseUrl;
	//Apify 작업 결과 데이터를 가져오는 API를 위한 기본 Output Url
	@Value("${apify.output-url}")
	private String baseOutputUrl;

	@Override
	public boolean supports(String url) {
		return url.contains(INSTAGRAM_URL);
	}

	@Override
	public CrawlCompletionResponse extractContents(Document document) {
		throw new UnsupportedOperationException("Jsoup을 이용한 크롤링이 불가능합니다.");
	}

	@Override
	@Cacheable(value = "contentCache", key = "caption")
	public CrawlCompletionResponse extractContentsUsingApify(String targetUrl) {
		try {
			// Apify 작업 실행 URL 생성 및 요청 데이터 준비
			String startTaskUrl = createStartTaskUrl();
			HttpEntity<String> entity = createHttpEntity(createRequestBody(targetUrl));

			// Apify 작업 실행 및 'defaultDatasetId' 추출
			String defaultDatasetId = getDefaultDatasetId(startTaskUrl, entity);

			// Output URL 생성
			String outputUrl = createOutputUrl(defaultDatasetId);

			// Output 추출
			JsonNode outputResponse = waitForOutputData(outputUrl);

			// Output 데이터에서 caption 필드 추출
			String caption = getCaptionFromOutput(outputResponse);

			//Output 데이터에서 images 필드 추출
			List<String> imageUrls = getImageUrlsFromOutput(outputResponse);

			// caption 반환
			return CrawlCompletionResponse.of(caption, imageUrls);
		} catch (Exception e) {
			throw new CrawlingException(SCRAPER_API_FAILED);
		}
	}

	private String createStartTaskUrl() {
		return String.format("%s?token=%s", baseUrl, apifyToken);
	}

	private String createOutputUrl(String defaultDatasetId) {
		return String.format("%s/datasets/%s/items?token=%s", baseOutputUrl, defaultDatasetId, apifyToken);
	}

	private String createRequestBody(String targetUrl) {
		return String.format("{\"directUrls\": [\"%s\"]}", targetUrl);
	}

	private HttpEntity<String> createHttpEntity(String requestBody) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(requestBody, headers);
	}

	private String getDefaultDatasetId(String startTaskUrl, HttpEntity<String> entity) throws Exception {
		ResponseEntity<String> response = restTemplate.exchange(startTaskUrl, HttpMethod.POST, entity, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode responseJson = objectMapper.readTree(response.getBody());
		JsonNode dataNode = responseJson.path("data");
		if (dataNode.isMissingNode() || !dataNode.has("defaultDatasetId")) {
			throw new CrawlingException(FIELD_NOT_FOUND);
		}

		return dataNode.get("defaultDatasetId").asText();
	}

	private JsonNode waitForOutputData(String outputUrl) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode outputResponse = objectMapper.createObjectNode();
		boolean isReady = false;
		while (!isReady) {
			ResponseEntity<String> response = restTemplate.getForEntity(outputUrl, String.class);
			outputResponse = objectMapper.readTree(response.getBody());
			if (outputResponse.isArray() && outputResponse.size() > 0) {
				isReady = true;
			} else {
				Thread.sleep(pollInterval);
			}
		}

		return outputResponse;
	}

	private String getCaptionFromOutput(JsonNode outputResponse) {
		JsonNode firstPost = outputResponse.get(0);
		if (!firstPost.has("caption")) {
			throw new CrawlingException(FIELD_NOT_FOUND);
		}

		return firstPost.get("caption").asText();
	}

	private List<String> getImageUrlsFromOutput(JsonNode outputResponse) {
		JsonNode firstPost = outputResponse.get(0);
		if (!firstPost.has("images")) {
			throw new CrawlingException(FIELD_NOT_FOUND);
		}
		List<String> imageUrls = new ArrayList<>();
		for (JsonNode imageNode : firstPost.get("images")) {
			imageUrls.add(imageNode.asText());
		}
		return imageUrls;
	}

}
