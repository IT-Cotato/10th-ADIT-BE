package com.adit.backend.infra.crawler.platform;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Jsoup;
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
public class NaverBlogCrawlingStrategy extends AbstractWebCrawlingStrategy {

	private static final Map<String, String> SKIN_TAGS = Map.of(
		"viewer", "div.se-viewer",
		"default", ".se-theme-default",
		"container", ".se-main-container",
		"post", ".post-area"
	);

	private final StringBuilder contentBuilder = new StringBuilder();

	@Override
	public boolean supports(String url) {
		return url.contains("blog.naver.com");
	}

	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public CrawlCompletionResponse extractContents(Document document) {
		Document innerDoc = getIframeDocument(document);
		String content = extractMainContent(innerDoc);
		String placeInfo = extractPlaceInfo(innerDoc);
		String combined = content + "\n\n[PLACE INFO]\n" + placeInfo;
		Elements contentElements = selectContentElements(innerDoc);
		return CrawlingUtil.getCrawlCompletionResponse(contentElements, combined);
	}

	private Document getIframeDocument(Document outerDoc) {
		try {
			Element iframe = outerDoc.selectFirst("iframe#mainFrame");
			if (iframe == null) {
				return outerDoc;
			}
			String src = iframe.attr("src");
			if (src == null || src.isBlank()) {
				return outerDoc;
			}
			String iframeUrl = src.startsWith("http") ? src : "https://blog.naver.com" + src;
			return Jsoup.connect(iframeUrl).userAgent("Mozilla/5.0").get();
		} catch (IOException e) {
			log.error("iframe 문서 파싱 실패: {}", e.getMessage(), e);
			return outerDoc;
		}
	}

	private String extractMainContent(Document document) {
		contentBuilder.setLength(0);
		try {
			String title = document.select("div.blog2_container h3.se_textarea, div.blog2_container h3.title").text();
			if (!title.isEmpty()) {
				contentBuilder.append("제목: ").append(title).append("\n\n");
			}
			Elements contentElements = selectContentElements(document);
			if (!contentElements.isEmpty()) {
				Element mainContent = contentElements.first();
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
			Elements placeElements = document.select("a.se-map-info.__se_link, div.se-map-info.__se_link");
			if (!placeElements.isEmpty()) {
				for (Element place : placeElements) {
					String text = place.text().trim();
					if (!text.isEmpty()) {
						placeBuilder.append(text).append("\n");
					}
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
		for (String selector : SKIN_TAGS.values()) {
			Elements elements = document.select(selector);
			if (!elements.isEmpty()) {
				return elements;
			}
		}
		return document.select("div.se-viewer");
	}
}