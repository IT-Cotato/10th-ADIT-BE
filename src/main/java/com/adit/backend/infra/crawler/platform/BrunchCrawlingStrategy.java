package com.adit.backend.infra.crawler.platform;

import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.infra.crawler.common.AbstractWebCrawlingStrategy;
import com.adit.backend.infra.crawler.util.CrawlingUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BrunchCrawlingStrategy extends AbstractWebCrawlingStrategy {

	private static final Map<String, String> BRUNCH_TAGS = Map.ofEntries(
		Map.entry("wrap", ".wrap_body"),
		Map.entry("main", ".wrap_body_frame")
	);

	private final StringBuilder contentBuilder = new StringBuilder();

	@Override
	public boolean supports(String url) {
		return url.contains("brunch.co.kr");
	}

	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public CrawlCompletionResponse extractContents(Document document) {
		String content = extractMainContent(document);
		String placeInfo = extractPlaceInfo(document);
		String combined = content + "\n\n[PLACE INFO]\n" + placeInfo;
		return CrawlingUtil.getCrawlCompletionResponse(selectContentElements(document), combined);
	}

	private String extractMainContent(Document document) {
		try {
			String title = document.select("h1, h2").text();
			if (!title.isEmpty()) {
				contentBuilder.append("제목: ").append(title).append("\n\n");
				log.info("제목 추출 성공: {}", title);
			} else {
				log.warn("제목 추출 실패 - URL: {}", document.location());
			}
			Elements contentElements = selectContentElements(document);
			if (!contentElements.isEmpty()) {
				Element mainContent = contentElements.first();
				log.info("본문 요소 선택 성공 - URL: {}", document.location());
				CrawlingUtil.removeUnnecessaryElements(mainContent);
				Elements textElements = mainContent.select("p, div:not(:has(p)), h1, h2, h3, h4, h5, h6");
				for (Element element : textElements) {
					String text = element.ownText().trim();
					if (!text.isEmpty() && text.length() > 10) {
						contentBuilder.append(text).append("\n");
					}
				}
			}
			return CrawlingUtil.preprocessText(contentBuilder.toString());
		} catch (Exception e) {
			log.error("본문 추출 중 오류 발생 - URL: {}, 에러: {}", document.location(), e.getMessage(), e);
			return "";
		}
	}

	private String extractPlaceInfo(Document document) {
		StringBuilder placeBuilder = new StringBuilder();
		try {
			Elements placeLinks = document.select("a.place");
			if (!placeLinks.isEmpty()) {
				for (Element place : placeLinks) {
					placeBuilder.append(place.text().trim()).append("\n");
				}
			} else {
				placeBuilder.append("PLACE 정보가 없습니다.");
			}
		} catch (Exception e) {
			log.error("PLACE 정보 추출 중 오류 - {}", e.getMessage(), e);
			return "PLACE 정보 추출 실패";
		}
		return placeBuilder.toString().trim();
	}

	private Elements selectContentElements(Document document) {
		for (Map.Entry<String, String> entry : BRUNCH_TAGS.entrySet()) {
			Elements elements = document.select(entry.getValue());
			if (!elements.isEmpty()) {
				log.info("선택자 '{}' 적용 성공 - URL: {}", entry.getValue(), document.location());
				return elements;
			}
		}
		return document.select("div.wrap_body");
	}
}