package com.adit.backend.domain.scraper.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.adit.backend.domain.scraper.Service.scraperService;
import com.adit.backend.domain.scraper.dto.response.scraperResponse;
import com.adit.backend.global.common.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/scraper")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class scraperController {

	private final scraperService scraperService;
	@PostMapping("/run")
	public ResponseEntity<ApiResponse<scraperResponse.scarperInfoDto>> runScraper(@RequestParam ("targetUrl") String targetUrl) {
		return ResponseEntity.ok(ApiResponse.success(scraperService.executeScraper(targetUrl)));
	}
}
