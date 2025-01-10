package com.adit.backend.domain.ai.controller;

import java.util.concurrent.CompletableFuture;

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

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenAiController {

	private final OpenAiService openAiService;
	private final ContentService contentService;

	@PostMapping("/summary")
	public ResponseEntity<ApiResponse<ContentListResponse>> pageSummary(
		@Valid @RequestBody final ContentExtractionRequest request) {
		return ResponseEntity.ok(ApiResponse.success(openAiService.analyzeCulturalInfo(request.url()).join()));
	}

	@PostMapping("/crawl")
	public ResponseEntity<ApiResponse<CompletableFuture<CrawlCompletionResponse>>> crawlPage(
		@Valid @RequestBody final ContentExtractionRequest request) {
		return ResponseEntity.ok(ApiResponse.success(contentService.extractContents(request.url())));
	}

}
