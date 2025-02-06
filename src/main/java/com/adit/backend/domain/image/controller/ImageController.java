package com.adit.backend.domain.image.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.adit.backend.domain.image.converter.ImageConverter;
import com.adit.backend.domain.image.dto.response.ImageResponseDto;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.service.command.ImageCommandService;
import com.adit.backend.domain.image.service.query.ImageQueryService;
import com.adit.backend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "Image API", description = "이미지를 관리하는 API 입니다. ()")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageController {

	private final ImageQueryService imageQueryService;
	private final ImageCommandService imageCommandService;
	private final ImageConverter imageConverter;

	@PostMapping
	@Operation(summary = "이미지 업로드", description = "이미지 URL을 기반으로 S3에 이미지를 업로드합니다.")
	public ResponseEntity<ApiResponse<String>> uploadImage(@RequestBody String url) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponse.success(imageCommandService.uploadImage(url)));
	}

	@GetMapping("/{imageId}")
	@Operation(summary = "특정 이미지 조회", description = "이미지 ID를 통해 특정 이미지를 조회합니다.")
	public ResponseEntity<ApiResponse<ImageResponseDto>> getImage(@PathVariable Long imageId) {
		Image image = imageQueryService.getImageById(imageId);
		return ResponseEntity.ok(ApiResponse.success(imageConverter.toResponse(image)));
	}

	@DeleteMapping("/{imageId}")
	@Operation(summary = "이미지 삭제", description = "이미지 ID를 통해 이미지를 삭제합니다.")
	public ResponseEntity<ApiResponse<String>> deleteImage(@PathVariable Long imageId) {
		imageCommandService.deleteImage(imageId);
		return ResponseEntity.ok(ApiResponse.success("이미지가 성공적으로 삭제되었습니다."));
	}

	@Operation(
		summary = "이미지 업데이트",
		description = "이미지 ID와 새로운 이미지 정보를 받아 해당 이미지를 업데이트한 후 업데이트된 이미지를 반환합니다."
	)
	@PutMapping("/{imageId}")
	public ResponseEntity<ApiResponse<ImageResponseDto>> updateImage(
		@PathVariable Long imageId, MultipartFile newImage) {
		return ResponseEntity.ok(ApiResponse.success(imageCommandService.updateImage(imageId, newImage)));
	}
}
