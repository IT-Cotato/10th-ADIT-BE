package com.adit.backend.domain.image.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.image.dto.request.ImageRequestDto;
import com.adit.backend.domain.image.dto.response.ImageResponseDto;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.service.ImageService;
import com.adit.backend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
@Hidden
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageController {

	private final ImageService imageService;

	// 이미지 업로드 API
	@PostMapping
	public ResponseEntity<ApiResponse<ImageResponseDto>> uploadImage(@Valid @RequestBody ImageRequestDto requestDto) {
		// 이미지 정보를 받아서 저장
		Image image = imageService.uploadImage(requestDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(ImageResponseDto.from(image)));
	}

	// 특정 이미지 조회 API
	@GetMapping("/{imageId}")
	public ResponseEntity<ApiResponse<ImageResponseDto>> getImage(@PathVariable Long imageId) {
		// 이미지 ID로 조회
		Image image = imageService.getImageById(imageId);
		return ResponseEntity.ok(ApiResponse.success(ImageResponseDto.from(image)));
	}

	// 이미지 삭제 API
	@DeleteMapping("/{imageId}")
	public ResponseEntity<ApiResponse<String>> deleteImage(@PathVariable Long imageId) {
		// 이미지 ID로 삭제
		imageService.deleteImage(imageId);
		return ResponseEntity.ok(ApiResponse.success("Image deleted successfully"));
	}
}
