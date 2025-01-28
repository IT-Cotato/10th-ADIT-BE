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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		if (url == null || url.isEmpty()) {
			log.warn("[Crawl] Instagram URL이 비어있음");
			return false;
		}
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
			log.debug("[Crawl] Instagram API 크롤링 시작: {}", targetUrl);

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

			log.debug("[Crawl] Instagram API 크롤링 완료");
			return CrawlCompletionResponse.of(caption, imageUrls);
		} catch (Exception e) {
			log.error("[Crawl] Instagram API 크롤링 실패: {}", e.getMessage());
			throw new CrawlingException(INSTAGRAM_API_CONNECTION_FAILED);
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
		try {
			ResponseEntity<String> response = restTemplate.exchange(startTaskUrl, HttpMethod.POST, entity, String.class);
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode responseJson = objectMapper.readTree(response.getBody());
			JsonNode dataNode = responseJson.path("data");
			if (dataNode.isMissingNode() || !dataNode.has("defaultDatasetId")) {
				log.error("[Crawl] Instagram 데이터셋 ID를 찾을 수 없음");
				throw new CrawlingException(INSTAGRAM_DATASET_NOT_FOUND);
			}
			String datasetId = dataNode.get("defaultDatasetId").asText();
			log.debug("[Crawl] Instagram 데이터셋 ID 추출 완료: {}", datasetId);
			return datasetId;
		} catch (Exception e) {
			log.error("[Crawl] Instagram 데이터셋 ID 추출 실패: {}", e.getMessage());
			throw new CrawlingException(INSTAGRAM_PARSING_FAILED);
		}
	}

	private JsonNode waitForOutputData(String outputUrl) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode outputResponse = objectMapper.createObjectNode();
		boolean isReady = false;
		int attempts = 0;
		while (!isReady) {
			ResponseEntity<String> response = restTemplate.getForEntity(outputUrl, String.class);
			outputResponse = objectMapper.readTree(response.getBody());
			if (outputResponse.isArray() && outputResponse.size() > 0) {
				log.debug("[Crawl] Instagram 데이터 추출 완료");
				isReady = true;
			} else {
				attempts++;
				log.debug("[Crawl] Instagram 데이터 대기 중... (시도: {})", attempts);
				Thread.sleep(pollInterval);
			}
		}
		return outputResponse;
	}

	private String getCaptionFromOutput(JsonNode outputResponse) {
		JsonNode firstPost = outputResponse.get(0);
		if (!firstPost.has("caption")) {
			log.error("[Crawl] Instagram 캡션을 찾을 수 없음");
			throw new CrawlingException(INSTAGRAM_CONTENT_EMPTY);
		}
		String caption = firstPost.get("caption").asText();
		log.debug("[Crawl] Instagram 캡션 추출 완료");
		return caption;
	}

	private List<String> getImageUrlsFromOutput(JsonNode outputResponse) {
		JsonNode firstPost = outputResponse.get(0);
		if (!firstPost.has("images")) {
			log.error("[Crawl] Instagram 이미지를 찾을 수 없음");
			throw new CrawlingException(INSTAGRAM_IMAGE_NOT_FOUND);
		}
		List<String> imageUrls = new ArrayList<>();
		for (JsonNode imageNode : firstPost.get("images")) {
			imageUrls.add(imageNode.asText());
		}
		log.debug("[Crawl] Instagram 이미지 URL 추출 완료: {} 개", imageUrls.size());
		return imageUrls;
	}
}
