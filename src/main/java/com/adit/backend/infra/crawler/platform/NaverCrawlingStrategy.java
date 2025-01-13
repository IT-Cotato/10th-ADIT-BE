package com.adit.backend.infra.crawler.platform;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.infra.crawler.common.AbstractWebCrawlingStrategy;
import com.adit.backend.infra.crawler.exception.CrawlingException;
import com.adit.backend.infra.crawler.util.CrawlingUtil;

import lombok.extern.slf4j.Slf4j;


/**
 * 네이버 플랫폼 크롤링 전략
 */
@Component
@Slf4j
public class NaverCrawlingStrategy extends AbstractWebCrawlingStrategy {

	public static final String NAVER_BLOG_URL = "blog.naver.com";
	public static final String BASE_URL = "https://blog.naver.com";
	public static final String IFRAME_TAG = "iframe#mainFrame";
	public static final String TEXT_TAG = "p, div:not(:has(p)), h1, h2, h3, h4, h5, h6";
	public static final String TITLE_TAG = "div.blog2_container h3.se_textarea, div.blog2_container h3.title";
	public static final String CONTENT_TAG = "div.se-main-container, div.post-area";
	public static final String PLACE_SEPARATOR = "\n\n[PLACE INFO]\n";
	public static final int MINIMUM_RECOGNIZED_CHARACTER = 10;

	@Override
	public boolean supports(String url) {
		return url.contains(NAVER_BLOG_URL);
	}

	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public CrawlCompletionResponse extractContents(Document document) {
		try {
			Document innerDoc = CrawlingUtil.getIframeDocument(document, IFRAME_TAG, BASE_URL);
			StringBuilder contentBuilder = new StringBuilder();
			extractTitle(innerDoc, contentBuilder);
			extractBody(innerDoc, contentBuilder);
			String content = CrawlingUtil.preprocessText(contentBuilder.toString());
			String placeInfo = CrawlingUtil.extractPlaceInfo(innerDoc);
			String combined = content + PLACE_SEPARATOR + placeInfo;
			Elements contentElements = selectContentElements(innerDoc);
			return CrawlingUtil.getCrawlCompletionResponse(contentElements, combined);

		} catch (IOException e) {
			log.error("[iframe 추출 중 오류] : {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		} catch (Exception e) {
			log.error("[본문 추출 중 오류 발생] : {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
	}

	private void extractTitle(Document document, StringBuilder contentBuilder) {
		String title = document.select(TITLE_TAG).text();
		if (!title.isEmpty()) {
			contentBuilder.append("제목: ").append(title).append("\n\n");
			log.info("[제목 추출 완료] : {}", title);
		} else {
			log.warn("[제목 추출 실패] : {}", document.location());
		}
	}

	private void extractBody(Document document, StringBuilder contentBuilder) {
		Elements contentElements = selectContentElements(document);
		if (!contentElements.isEmpty()) {
			Element mainContent = contentElements.first();
			log.info("[본문 요소 선택 성공] : {}", mainContent.cssSelector());
			CrawlingUtil.removeUnnecessaryElements(mainContent);
			Elements textElements = mainContent.select(TEXT_TAG);
			for (Element element : textElements) {
				String text = element.text().trim();
				if (text.length() > MINIMUM_RECOGNIZED_CHARACTER) {
					contentBuilder.append(text).append("\n");
				}
			}
		} else {
			log.warn("[본문 요소 선택 실패]");
		}
	}

	private Elements selectContentElements(Document document) {
		Elements elements = document.select(CONTENT_TAG);
		if (!elements.isEmpty()) {
			return elements;
		}
		log.warn("[선택자 추출 중 오류 발생] : 기본 선택자로 본문을 찾지 못했습니다");
		return new Elements();
	}
}
