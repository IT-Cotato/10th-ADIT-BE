package com.adit.backend.domain.place.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.place.dto.request.CommonPlaceRequestDto;
import com.adit.backend.domain.place.dto.response.CommonPlaceResponseDto;
import com.adit.backend.domain.place.dto.response.UserPlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.service.CommonPlaceService;
import com.adit.backend.global.common.ApiResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Hidden
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonPlaceController {

	private final CommonPlaceService commonPlaceService;

	// 장소 생성 API
	@PostMapping
	public ResponseEntity<ApiResponse<CommonPlaceResponseDto>> createPlace(
		@Valid @RequestBody CommonPlaceRequestDto requestDto) {
		// 장소 정보를 받아 CommonPlaceService에서 처리
		CommonPlace place = commonPlaceService.createPlace(requestDto);
		// 생성된 장소를 응답으로 반환
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(CommonPlaceResponseDto.from(place)));
	}

	// 장소 수정 API
	@PutMapping("/{placeId}")
	public ResponseEntity<ApiResponse<CommonPlaceResponseDto>> updatePlace(@PathVariable Long placeId,
		@Valid @RequestBody CommonPlaceRequestDto requestDto) {
		// ID로 기존 장소를 찾아 수정
		CommonPlace updatedPlace = commonPlaceService.updatePlace(placeId, requestDto);
		// 수정된 장소를 응답으로 반환
		return ResponseEntity.ok(ApiResponse.success(CommonPlaceResponseDto.from(updatedPlace)));
	}

	// 장소 삭제 API
	@DeleteMapping("/{placeId}")
	public ResponseEntity<ApiResponse<String>> deletePlace(@PathVariable Long placeId) {
		// ID로 장소를 삭제
		commonPlaceService.deletePlace(placeId);
		// 삭제 완료 메시지 응답
		return ResponseEntity.ok(ApiResponse.success("Place deleted successfully"));
	}

	// 카테고리 기반으로 장소 찾기 API
	@GetMapping("/category")
	public ResponseEntity<ApiResponse<List<UserPlaceResponseDto>>> getPlaceByCategory(@RequestParam String subCategory, @RequestParam Long userID){
		if (subCategory.isBlank()){
			throw new BusinessException(GlobalErrorCode.NOT_VALID_ERROR);
		}
		if (userID <= 0){
			throw new BusinessException(GlobalErrorCode.NOT_VALID_ERROR);
		}
		List<UserPlaceResponseDto> placeByCategory = commonPlaceService.getPlaceByCategory(subCategory, userID);

		return ResponseEntity.ok(ApiResponse.success(placeByCategory));
	}

	//인기 기반으로 장소 찾기 API
	@GetMapping("/popular")
	public ResponseEntity<ApiResponse<List<CommonPlaceResponseDto>>> getPlaceByPopular(){
		List<CommonPlaceResponseDto> placeByPopular = commonPlaceService.getPlaceByPopular();
		return ResponseEntity.ok(ApiResponse.success(placeByPopular));
	}

	//저장된 장소 찾기 API
	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<List<UserPlaceResponseDto>>> getSavedPlace(@PathVariable Long userId){
		List<UserPlaceResponseDto> savedPlace = commonPlaceService.getSavedPlace(userId);
		return ResponseEntity.ok(ApiResponse.success(savedPlace));
	}

	//특정 장소 상세 정보 찾기 API
	@GetMapping("/detail")
	public ResponseEntity<ApiResponse<CommonPlaceResponseDto>> getDetailedPlace(@RequestParam String businessName){
		CommonPlaceResponseDto detailedPlace = commonPlaceService.getDetailedPlace(businessName);
		return ResponseEntity.ok(ApiResponse.success(detailedPlace));
	}

	//현재 위치 기반 장소 찾기 API
	@GetMapping("/location/{userId}")
	public ResponseEntity<ApiResponse<List<UserPlaceResponseDto>>> getPlaceByLocation(@RequestParam double latitude, @RequestParam double longitude, @PathVariable Long userId){
		List<UserPlaceResponseDto> placeByLocation = commonPlaceService.getPlaceByLocation(latitude,longitude, userId);
		return ResponseEntity.ok(ApiResponse.success(placeByLocation));
	}

	//주소 기반 장소 찾기 API
	@GetMapping("/address/{userId}")
	public ResponseEntity<ApiResponse<List<UserPlaceResponseDto>>> getPlaceByAddress(@RequestParam List<String> address, @PathVariable Long userId){
		List<UserPlaceResponseDto> placeByAddress = commonPlaceService.getPlaceByAddress(address, userId);
		return ResponseEntity.ok(ApiResponse.success(placeByAddress));
	}

	//장소 방문 여부 표시 API
	@PutMapping("/visit/{userPlaceId}")
	public ResponseEntity<ApiResponse<String>> checkVisitedPlace(@PathVariable Long userPlaceId){
		commonPlaceService.checkVisitedPlace(userPlaceId);
		return ResponseEntity.ok(ApiResponse.success("visit sign successfully"));
	}

	//친구 기반 장소 찾기 API
	@GetMapping("/{userId}/friend")
	public ResponseEntity<ApiResponse<List<UserPlaceResponseDto>>> getPlaceByFriend(@PathVariable Long userId){
		List<UserPlaceResponseDto> placeByFriend = commonPlaceService.getPlaceByFriend(userId);
		return ResponseEntity.ok(ApiResponse.success(placeByFriend));
	}
}
