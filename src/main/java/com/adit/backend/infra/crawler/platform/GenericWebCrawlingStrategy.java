package com.adit.backend.infra.crawler.platform;

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
public class GenericWebCrawlingStrategy extends AbstractWebCrawlingStrategy {
	private final StringBuilder contentBuilder = new StringBuilder();

	@Override
	public boolean supports(String url) {
		return !url.contains("tistory.com") && !url.contains("blog.naver.com") && !url.contains("brunch.co.kr");
	}

	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public CrawlCompletionResponse extractContents(Document document) {
		String content = extractMainContent(document);
		return CrawlingUtil.getCrawlCompletionResponse(document.select(".entry-content"), content);
	}

	private String extractMainContent(Document document) {
		try {
			String title = document.select("title, h1, h2").first() != null
				? document.select("title, h1, h2").first().text()
				: "";
			if (!title.isEmpty()) {
				contentBuilder.append("제목: ").append(title).append("\n\n");
				log.info("제목 추출 성공: {}", title);
			} else {
				log.warn("제목 추출 실패 - URL: {}", document.location());
			}

			Element bodyElement = document.selectFirst("body");
			if (bodyElement != null) {
				CrawlingUtil.removeUnnecessaryElements(bodyElement);
				Elements textElements = bodyElement.select("p, div:not(:has(p)), h1, h2, h3, h4, h5, h6");
				for (Element element : textElements) {
					String text = element.ownText().trim();
					if (!text.isEmpty() && text.length() > 10) {
						contentBuilder.append(text).append("\n");
					}
				}
			} else {
				log.warn("body 태그 선택 실패 - URL: {}", document.location());
			}

			return CrawlingUtil.preprocessText(contentBuilder.toString());

		} catch (Exception e) {
			log.error("본문 추출 중 오류 - URL: {}, 에러: {}", document.location(), e.getMessage(), e);
			return "";
		}
	}
}