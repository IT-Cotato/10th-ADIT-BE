package com.adit.backend.infra.crawler.platform;

import java.io.IOException;

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

	private static final String BASE_URL = "https://blog.naver.com";

	@Override
	public boolean supports(String url) {
		return url.contains("blog.naver.com");
	}

	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public CrawlCompletionResponse extractContents(Document document) {
		// 1. iframe 내부 Document 가져오기
		Document innerDoc = getIframeDocument(document);

		// 2. 본문 텍스트 추출
		String content = extractMainContent(innerDoc);

		// 3. 장소 정보 (예시)
		String placeInfo = extractPlaceInfo(innerDoc);

		// 4. 데이터 통합
		String combined = content + "\n\n[PLACE INFO]\n" + placeInfo;

		// 5. 반환 (본문 Elements 포함)
		Elements contentElements = selectContentElements(innerDoc);
		return CrawlingUtil.getCrawlCompletionResponse(contentElements, combined);
	}

	/**
	 * iframe#mainFrame 내부 HTML을 가져옴.
	 */
	private Document getIframeDocument(Document outerDoc) {
		try {
			Element iframe = outerDoc.selectFirst("iframe#mainFrame");
			if (iframe == null) {
				log.warn("iframe#mainFrame을 찾지 못했습니다.");
				return outerDoc; // iframe이 없으면 바깥 문서 그대로 사용
			}

			String src = iframe.attr("src");
			if (src == null || src.isBlank()) {
				log.warn("iframe src가 비어 있습니다.");
				return outerDoc;
			}

			String iframeUrl = src.startsWith("http") ? src : BASE_URL + src;
			log.info("iframe URL: {}", iframeUrl);

			return Jsoup.connect(iframeUrl).userAgent("Mozilla/5.0").get();
		} catch (IOException e) {
			log.error("iframe 문서 파싱 실패: {}", e.getMessage(), e);
			return outerDoc; // 실패 시 바깥 문서 반환
		}
	}

	/**
	 * 본문 텍스트를 추출.
	 */
	private String extractMainContent(Document document) {
		StringBuilder contentBuilder = new StringBuilder();

		try {
			// 제목 추출
			String title = document.select("div.blog2_container h3.se_textarea, div.blog2_container h3.title").text();
			if (!title.isEmpty()) {
				contentBuilder.append("제목: ").append(title).append("\n\n");
				log.info("제목 추출 성공: {}", title);
			} else {
				log.warn("제목 추출 실패");
			}

			// 본문 요소 선택
			Elements contentElements = selectContentElements(document);
			if (!contentElements.isEmpty()) {
				Element mainContent = contentElements.first();
				log.info("본문 요소 선택 성공");

				// 불필요한 태그 제거
				CrawlingUtil.removeUnnecessaryElements(mainContent);

				// 텍스트 추출
				Elements textElements = mainContent.select("p, div:not(:has(p)), h1, h2, h3, h4, h5, h6");
				for (Element element : textElements) {
					String text = element.text().trim();
					if (!text.isEmpty() && text.length() > 10) {
						contentBuilder.append(text).append("\n");
					}
				}
			} else {
				log.warn("본문 요소 선택 실패");
			}

		} catch (Exception e) {
			log.error("본문 추출 중 오류 발생: {}", e.getMessage(), e);
		}

		return CrawlingUtil.preprocessText(contentBuilder.toString());
	}

	/**
	 * 장소 정보를 추출 (예시).
	 */
	private String extractPlaceInfo(Document document) {
		StringBuilder placeBuilder = new StringBuilder();

		try {
			Elements placeElements = document.select("a.se-map-info.__se_link, div.se-map-info.__se_link");
			for (Element place : placeElements) {
				String text = place.text().trim();
				if (!text.isEmpty()) {
					placeBuilder.append(text).append("\n");
				}
			}
		} catch (Exception e) {
			log.error("PLACE 정보 추출 중 오류: {}", e.getMessage(), e);
		}

		return placeBuilder.toString().trim();
	}

	/**
	 * SKIN_TAGS를 순회하여 본문 요소를 선택.
	 */
	private Elements selectContentElements(Document document) {
		Elements elements = document.select("div.se-main-container, div.post-area");
		if (!elements.isEmpty()) {
			return elements;
		}
		log.warn("기본 선택자로 본문을 찾지 못했습니다.");
		return new Elements(); // 빈 Elements 반환
	}
}
