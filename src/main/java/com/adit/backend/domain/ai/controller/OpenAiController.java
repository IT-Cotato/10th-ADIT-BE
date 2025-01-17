package com.adit.backend.domain.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.ai.dto.request.ContentExtractionRequest;
import com.adit.backend.domain.ai.dto.response.ContentListResponse;
import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.domain.ai.service.ContentService;
import com.adit.backend.domain.ai.service.OpenAiService;
import com.adit.backend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Tag(name = "AI Summary API", description = "웹 페이지 크롤링 및 OpenAI를 활용한 문화/외식 정보 추출 API")
public class OpenAiController {

	private final OpenAiService openAiService;
	private final ContentService contentService;

	@Operation(
		summary = "URL 기반 문화/외식 정보 분석",
		description = "입력된 URL의 웹 페이지를 크롤링하여 OpenAI를 통해 문화/외식 관련 정보를 추출하고 구조화된 데이터로 반환합니다."
	)
	@PostMapping("/summary")
	public ResponseEntity<ApiResponse<ContentListResponse>> summaryPage(
		@Parameter(description = "분석할 웹 페이지 URL 정보", required = true)
		@Valid @RequestBody final ContentExtractionRequest request) {
		return ResponseEntity.ok(ApiResponse.success(openAiService.analyzeCulturalInfo(request.url())));
	}

	@Operation(
		summary = "URL 크롤링 테스트",
		description = "입력된 URL의 웹 페이지 크롤링을 수행하여 추출된 원본 컨텐츠를 반환합니다."
	)
	@PostMapping("/crawl")
	public ResponseEntity<ApiResponse<CrawlCompletionResponse>> crawlPage(
		@Parameter(description = "크롤링할 웹 페이지 URL 정보", required = true)
		@Valid @RequestBody final ContentExtractionRequest request) {
		return ResponseEntity.ok(ApiResponse.success(contentService.extractContents(request.url()).join()));
	}
}
