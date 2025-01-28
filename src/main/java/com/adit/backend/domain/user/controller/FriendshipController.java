package com.adit.backend.domain.user.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.user.dto.request.FriendRequestDto;
import com.adit.backend.domain.user.dto.response.FriendshipResponseDto;
import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.principal.PrincipalDetails;
import com.adit.backend.domain.user.service.command.FriendCommandService;
import com.adit.backend.domain.user.service.query.FriendQueryService;
import com.adit.backend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendshipController {

	private final FriendCommandService friendCommandService;
	private final FriendQueryService friendQueryService;

	//친구 요청 보내기 API
	@Operation(summary = "친구 요청 보내기", description = "fromUserId, toUserId 정보를 바탕으로 정방향,역방향 관계 저장")
	@PostMapping("/send")
	public ResponseEntity<ApiResponse<FriendshipResponseDto>> sendFriendRequest(
		@Valid @RequestBody FriendRequestDto requestDto) {
		// 친구 요청을 처리하여 응답 반환
		FriendshipResponseDto savedForwardRequest = friendCommandService.sendFriendRequest(requestDto);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponse.success(savedForwardRequest));
	}

	 // 친구 요청 수락 API
	@Operation(summary = "친구 요청 수락", description = "requestId에 해당하는 요청을 true 로 저장")
	@PostMapping("/accept")
	public ResponseEntity<ApiResponse<String>> acceptFriendRequest(@RequestParam Long requestId) {
		// 요청 ID로 친구 요청을 수락 처리
		friendCommandService.acceptFriendRequest(requestId);
		return ResponseEntity.ok(ApiResponse.success("Friend request accepted"));
	}

	// 친구 요청 거절 API
	@Operation(summary = "친구 요청 거절", description = "requestId에 해당하는 요청과 그에 따른 역방향 요청 데이터 삭제")
	@PostMapping("/reject")
	public ResponseEntity<ApiResponse<String>> rejectFriendRequest(@RequestParam Long requestId) {
		// 요청 ID로 친구 요청을 거절 처리
		friendCommandService.rejectFriendRequest(requestId);
		return ResponseEntity.ok(ApiResponse.success("Friend request rejected"));
	}

	// 친구 삭제 API
	@Operation(summary = "친구 삭제", description = "friendId에 해당하는 친구를 삭제")
	@DeleteMapping("/{friendId}")
	public ResponseEntity<ApiResponse<String>> removeFriend(@PathVariable Long friendId,
	@AuthenticationPrincipal PrincipalDetails principalDetails ) {
		// 친구 관계를 삭제
		Long userId = principalDetails.getUserId();
		friendCommandService.removeFriend(userId, friendId);
		return ResponseEntity.ok(ApiResponse.success("Friend removed"));
	}

	//친구 요청 목록 확인 API
	@Operation(summary = "친구 요청 목록 조회", description = "userId에 해당하는 사용자가 보내거나 받은 친구 요청 조회")
	@GetMapping("/{userId}/check")
	public ResponseEntity<ApiResponse<Map<String, List<FriendshipResponseDto>>>> checkRequest(
		@PathVariable Long userId) {
		Map<String, List<FriendshipResponseDto>> requests = friendQueryService.checkRequest(userId);
		return ResponseEntity.ok(ApiResponse.success(requests));
	}

	//친구 목록 확인 API
	@Operation(summary = "친구 목록 조회", description = "userId에 해당하는 사용자의 친구 조회")
	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<List<UserResponse.InfoDto>>> findFriends(@AuthenticationPrincipal
		(expression = "user") User user){
		List<UserResponse.InfoDto> friendList = friendQueryService.findFriends(user.getId());
		return ResponseEntity.ok(ApiResponse.success(friendList));
	}

	//사용자 검색 API
	@Operation(summary = "사용자 검색", description = "해당 NickName 에 해당하는 사용자 조회")
	@GetMapping("/search")
	public ResponseEntity<ApiResponse<Map<String, UserResponse.InfoDto>>> findUser(@RequestParam String NickName,
		@AuthenticationPrincipal PrincipalDetails principalDetails) {
		Long userId = principalDetails.getUserId();
		Map<String, UserResponse.InfoDto> searchedUser = friendQueryService.findUser(NickName, userId);
		return ResponseEntity.ok(ApiResponse.success(searchedUser));
	}
}
