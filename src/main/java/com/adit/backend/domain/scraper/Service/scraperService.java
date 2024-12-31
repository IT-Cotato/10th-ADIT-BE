package com.adit.backend.domain.scraper.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.adit.backend.domain.scraper.dto.response.scraperResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class scraperService {

	private final RestTemplate restTemplate;

	@Value("${apify.token}")
	private String apifyToken;

	@Value("${apify.poll-interval}")
	private long pollInterval;

	@Value("${apify.base-url}")
	private String baseUrl;

	@Value("${apify.output-url}")
	private String baseOutputUrl;

	public scraperResponse.scarperInfoDto executeScraper(String targetUrl) {
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

			// caption 반환
			return new scraperResponse.scarperInfoDto(caption);

		} catch (Exception e) {
			throw new RuntimeException("Error while executing scraper: " + e.getMessage(), e);
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
			throw new RuntimeException("Invalid response: Missing 'defaultDatasetId' field");
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
			throw new RuntimeException("Invalid response: Missing 'caption' field");
		}

		return firstPost.get("caption").asText();
	}
}
