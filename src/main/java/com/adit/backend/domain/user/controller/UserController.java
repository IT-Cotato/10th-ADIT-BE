package com.adit.backend.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.user.dto.request.UserRequest;
import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.service.command.UserCommandService;
import com.adit.backend.domain.user.service.query.UserQueryService;
import com.adit.backend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserController {

	private final UserCommandService userCommandService;
	private final UserQueryService userQueryService;

	@PostMapping("/nickname")
	@SecurityRequirement(name = "accessTokenAuth")
	public ResponseEntity<ApiResponse<UserResponse.InfoDto>> changeNickname(
		@RequestHeader("Authorization") String accessCode, @RequestBody @Valid UserRequest.NicknameDto request) {
		return ResponseEntity.ok(
			ApiResponse.success(userCommandService.changeNickname(accessCode, request.nickname())));
	}

	@PostMapping("/validation")
	@SecurityRequirement(name = "accessTokenAuth")
	public ResponseEntity<ApiResponse<String>> validateNickname(
		@RequestBody @Valid UserRequest.NicknameDto request) {
		userQueryService.validateDuplicateNicknames(request.nickname());
		return ResponseEntity.ok(ApiResponse.success("사용 가능한 닉네임입니다."));
	}
}
