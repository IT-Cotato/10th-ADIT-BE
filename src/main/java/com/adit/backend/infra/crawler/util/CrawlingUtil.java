package com.adit.backend.infra.crawler.util;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.infra.crawler.exception.CrawlingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrawlingUtil {

	private static final int CHUNK_SIZE = 500;
	private static final int BATCH_SIZE = 4;

	public static String preprocessText(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		return text.replaceAll("\\b(https?|ftp|file)://\\S+\\b", "")
			.replaceAll("<[^>]+>", "")
			.replaceAll("\\[.*?\\]", "")
			.replaceAll("\\(.*?\\)", "")
			.replaceAll("[\\p{So}\\p{Sk}]", "")
			.replaceAll("[\\r\\n]+", "\n")
			.replaceAll("\\s{2,}", " ")
			.trim();
	}

	public static void removeUnnecessaryElements(Element mainContent) {
		try {
			mainContent.select("script, style, button, input, textarea").remove();
			log.info("불필요한 태그 제거 완료 - URL: {}", mainContent.baseUri());
		} catch (Exception e) {
			log.error("불필요 태그 제거 실패 - 에러: {}", e.getMessage(), e);
		}
	}

	public static List<String> splitIntoChunks(String text) {
		List<String> chunks = new ArrayList<>();
		String[] sentences = text.split("(?<=[.!?]\\s)");
		StringBuilder currentChunk = new StringBuilder();

		for (String sentence : sentences) {
			if (currentChunk.length() + sentence.length() > CHUNK_SIZE) {
				if (currentChunk.length() > 0) {
					chunks.add(currentChunk.toString().trim());
					currentChunk.setLength(0);
				}
			}
			currentChunk.append(sentence).append(" ");
		}

		if (currentChunk.length() > 0) {
			chunks.add(currentChunk.toString().trim());
		}

		log.info("본문을 청크로 분할 완료 - 총 청크 개수: {}, URL 길이: {}", chunks.size(), text.length());
		return chunks;
	}

	public static List<String> processBatchChunks(List<String> chunks) {
		List<String> results = new ArrayList<>();

		for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
			List<String> batch = chunks.subList(i, Math.min(chunks.size(), i + BATCH_SIZE));
			String combinedText = String.join("\n", batch);

			if (!combinedText.trim().isEmpty()) {
				results.add(combinedText);
				log.debug("청크 배치 처리 완료 - 배치 크기: {}, 텍스트 길이: {}", batch.size(), combinedText.length());
			}
		}

		return results;
	}

	public static List<String> extractImageSrcList(Elements elements) {
		if (elements == null) {
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
		List<String> imageSrcList = new ArrayList<>();
		Elements imgElements = elements.select("img");
		for (Element img : imgElements) {
			String highResUrl = img.attr("data-lazy-src");
			if (highResUrl.isEmpty()) {
				highResUrl = img.attr("data-origin");
			}
			if (highResUrl.isEmpty()) {
				highResUrl = img.attr("src");
			}
			if (highResUrl.contains("?type=")) {
				highResUrl = highResUrl.split("\\?type=")[0];
			}
			if (!highResUrl.isEmpty()) {
				imageSrcList.add(highResUrl + "?type=w966");
			}
		}
		return imageSrcList;
	}

	public static CrawlCompletionResponse getCrawlCompletionResponse(Elements elements, String contents) {
		if (contents.isEmpty()) {
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
		log.debug("추출된 원본 컨텐츠 (전체 길이: {}자): {}", contents.length(), contents);
		List<String> chunks = splitIntoChunks(contents);
		List<String> imageSrcList = extractImageSrcList(elements);
		return CrawlCompletionResponse.of(
			String.join("", processBatchChunks(chunks)),
			imageSrcList);
	}
}