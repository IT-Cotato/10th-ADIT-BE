package com.adit.backend.domain.place.controller;

import java.util.List;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.domain.place.service.CommonPlaceService;
import com.adit.backend.global.common.ApiResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Validated
@Tag(name = "Place API", description = "장소 생성, 수정, 삭제, 조회할 수 있는 API 입니다")
public class CommonPlaceController {

	private final CommonPlaceService commonPlaceService;

	// 장소 생성 API
	@Operation(summary = "장소 생성", description = "카카오 맵 키워드 검색 후 CommonPlace, UserPlace 에 장소를 저장합니다")
	@PostMapping("/{userId}/create")
	public ResponseEntity<ApiResponse<PlaceResponseDto>> createPlace(
		@Valid @RequestBody CommonPlaceRequestDto requestDto,@PathVariable@Min(1) Long userId,@RequestParam String memo) {
		if (memo.isBlank()){
			throw new BusinessException(GlobalErrorCode.NOT_VALID_ERROR);
		}
		// 장소 정보를 받아 CommonPlaceService에서 처리
		CommonPlace place = commonPlaceService.createCommonPlace(requestDto);
		UserPlace userPlace = commonPlaceService.createUserPlace(place, userId, memo);

		// 생성된 장소를 응답으로 반환
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(PlaceResponseDto.commonPlace(place)));
	}

	// 장소 수정 API
	@Operation(summary = "장소 수정", description = "Commonplace 의 장소를 수정합니다")
	@PutMapping("/{placeId}")
	public ResponseEntity<ApiResponse<PlaceResponseDto>> updatePlace(@PathVariable @Min(1) Long placeId,
		@Valid @RequestBody CommonPlaceRequestDto requestDto) {
		// ID로 기존 장소를 찾아 수정
		CommonPlace updatedPlace = commonPlaceService.updatePlace(placeId, requestDto);
		// 수정된 장소를 응답으로 반환
		return ResponseEntity.ok(ApiResponse.success(PlaceResponseDto.commonPlace(updatedPlace)));
	}

	// 장소 삭제 API
	@Operation(summary = "장소 삭제", description = "userPlaceId에 해당하는 장소 삭제")
	@DeleteMapping("/{userPlaceId}")
	public ResponseEntity<ApiResponse<String>> deletePlace(@PathVariable @Min(1)Long userPlaceId) {
		// ID로 장소를 삭제
		commonPlaceService.deletePlace(userPlaceId);
		// 삭제 완료 메시지 응답
		return ResponseEntity.ok(ApiResponse.success("Place deleted successfully"));
	}

	// 카테고리 기반으로 장소 찾기 API
	@Operation(summary = "카테고리로 장소 조회", description = "userId에 해당하는 사용자가 가진 장소 중 특정 카테고리에 해당하는 장소 조회")
	@GetMapping("/{userId}/category")
	public ResponseEntity<ApiResponse<List<PlaceResponseDto>>> getPlaceByCategory(@RequestParam String subCategory, @PathVariable @Min(1) Long userId){
		if (subCategory.isBlank()){
			throw new BusinessException(GlobalErrorCode.NOT_VALID_ERROR);
		}

		List<PlaceResponseDto> placeByCategory = commonPlaceService.getPlaceByCategory(subCategory, userId);

		return ResponseEntity.ok(ApiResponse.success(placeByCategory));
	}

	//인기 기반으로 장소 찾기 API
	@Operation(summary = "인기순으로 장소 조회", description = "전체 장소 중 bookmarkCount 가 높은 순서대로 1~5위 장소 조회")
	@GetMapping("/popular")
	public ResponseEntity<ApiResponse<List<PlaceResponseDto>>> getPlaceByPopular(){
		List<PlaceResponseDto> placeByPopular = commonPlaceService.getPlaceByPopular();
		return ResponseEntity.ok(ApiResponse.success(placeByPopular));
	}

	//저장된 장소 찾기 API
	@Operation(summary = "저장된 장소 조회", description = "userId에 해당하는 사용자가 저장한 장소 조회")
	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<List<PlaceResponseDto>>> getSavedPlace(@PathVariable@Min(1) Long userId){
		List<PlaceResponseDto> savedPlace = commonPlaceService.getSavedPlace(userId);
		return ResponseEntity.ok(ApiResponse.success(savedPlace));
	}

	//특정 장소 상세 정보 찾기 API
	@Operation(summary = "특정 장소 상세 정보 조회", description = "해당 placeName(상호명)을 가진 장소 조회")
	@GetMapping("/detail")
	public ResponseEntity<ApiResponse<PlaceResponseDto>> getDetailedPlace(@RequestParam String placeName){
		if (placeName.isBlank()){
			throw new BusinessException(GlobalErrorCode.NOT_VALID_ERROR);
		}
		PlaceResponseDto detailedPlace = commonPlaceService.getDetailedPlace(placeName);
		return ResponseEntity.ok(ApiResponse.success(detailedPlace));
	}

	//현재 위치 기반 장소 찾기 API
	@Operation(summary = "사용자 위치로 장소 조회", description = "userId에 해당하는 사용자가 가진 장소 중 사용자의 위치와 가까운 순으로 장소 조회")
	@GetMapping("/{userId}/location")
	public ResponseEntity<ApiResponse<List<PlaceResponseDto>>> getPlaceByLocation(@RequestParam @DecimalMin("33.0") @DecimalMax("43.0") double latitude, @RequestParam @DecimalMin("124.0") @DecimalMax("132.0") double longitude, @PathVariable@Min(1) Long userId){
		List<PlaceResponseDto> placeByLocation = commonPlaceService.getPlaceByLocation(latitude,longitude, userId);
		return ResponseEntity.ok(ApiResponse.success(placeByLocation));
	}

	//주소 기반 장소 찾기 API
	@Operation(summary = "주소로 장소 조회", description = "userId에 해당하는 사용자가 가진 장소 중 address 를 포함하고 있는 장소 조회")
	@GetMapping("/{userId}/address")
	public ResponseEntity<ApiResponse<List<PlaceResponseDto>>> getPlaceByAddress(@RequestParam List<String> address, @PathVariable@Min(1) Long userId){
		if (address.stream().anyMatch(String::isBlank)) {
			throw new BusinessException(GlobalErrorCode.NOT_VALID_ERROR);
		}
		List<PlaceResponseDto> placeByAddress = commonPlaceService.getPlaceByAddress(address, userId);
		return ResponseEntity.ok(ApiResponse.success(placeByAddress));
	}

	//장소 방문 여부 표시 API
	@Operation(summary = "장소 방문 표시", description = "userPlaceId에 해당하는 장소 방문 표시")
	@PutMapping("/{userPlaceId}/visit")
	public ResponseEntity<ApiResponse<String>> checkVisitedPlace(@PathVariable@Min(1) Long userPlaceId){
		commonPlaceService.checkVisitedPlace(userPlaceId);
		return ResponseEntity.ok(ApiResponse.success("visit sign successfully"));
	}

	//친구 기반 장소 찾기 API
	@Operation(summary = "친구 장소 조회", description = "userId에 해당하는 사용자의 친구가 저장한 장소 조회")
	@GetMapping("/{userId}/friend")
	public ResponseEntity<ApiResponse<List<PlaceResponseDto>>> getPlaceByFriend(@PathVariable@Min(1) Long userId){
		List<PlaceResponseDto> placeByFriend = commonPlaceService.getPlaceByFriend(userId);
		return ResponseEntity.ok(ApiResponse.success(placeByFriend));
	}

	//장소 메모 수정 API
	@Operation(summary = "장소 메모 수정", description = "userPlaceId에 해당하는 장소의 메모를 수정")
	@PutMapping("/{userPlaceId}/memo")
	public ResponseEntity<ApiResponse<PlaceResponseDto>> updateUserPlace(@PathVariable@Min(1) Long userPlaceId , @RequestParam String memo){
		if (memo.isBlank()){
			throw new BusinessException(GlobalErrorCode.NOT_VALID_ERROR);
		}
		PlaceResponseDto updateUserPlace = commonPlaceService.updateUserPlace(userPlaceId, memo);
		return ResponseEntity.ok(ApiResponse.success(updateUserPlace));
	}
}
