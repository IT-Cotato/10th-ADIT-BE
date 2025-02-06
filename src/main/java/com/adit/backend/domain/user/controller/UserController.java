package com.adit.backend.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.user.annotation.ChangeNicknameApiSpec;
import com.adit.backend.domain.user.annotation.DuplicateNicknameResponse;
import com.adit.backend.domain.user.annotation.SuccessNicknameResponse;
import com.adit.backend.domain.user.dto.request.UserRequest;
import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.service.command.UserCommandService;
import com.adit.backend.domain.user.service.query.UserQueryService;
import com.adit.backend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Tag(name = "User API", description = "사용자 정보 API")
public class UserController {

	private final UserCommandService userCommandService;
	private final UserQueryService userQueryService;

	@ChangeNicknameApiSpec
	@DuplicateNicknameResponse
	@PostMapping("/nickname")
	public ResponseEntity<ApiResponse<UserResponse.InfoDto>> changeNickname(
		@AuthenticationPrincipal(expression = "user") User user, @RequestBody @Valid UserRequest.NicknameDto request) {
		return ResponseEntity.ok(
			ApiResponse.success(userCommandService.changeNickname(user, request.nickname())));
	}

	@SuccessNicknameResponse
	@DuplicateNicknameResponse
	@PostMapping("/validation")
	@Operation(summary = "닉네임 중복 테스트", description = "닉네임 중복 및 제한사항을 테스트합니다.")
	public ResponseEntity<ApiResponse<String>> validateNickname(
		@RequestBody @Valid UserRequest.NicknameDto request) {
		userQueryService.validateDuplicateNicknames(request.nickname());
		return ResponseEntity.ok(ApiResponse.success("사용 가능한 닉네임입니다."));
	}

}
