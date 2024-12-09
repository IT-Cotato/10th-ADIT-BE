package com.adit.backend.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.user.dto.request.FriendRequestDto;
import com.adit.backend.domain.user.entity.FriendRelationship;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.repository.FriendRelationshipRepository;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.global.error.exception.BusinessException;
import com.adit.backend.global.error.exception.GlobalErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRelationshipService {

	private final FriendRelationshipRepository friendRelationshipRepository;
	private final UserRepository userRepository;

	// 친구 요청 보내기

	public FriendRelationship sendFriendRequest(FriendRequestDto requestDto) {
		User fromUser = userRepository.findById(requestDto.fromUser().getId())
			.orElseThrow(() -> new BusinessException("User not found", GlobalErrorCode.NOT_FOUND_ERROR));
		User toUser = userRepository.findById(requestDto.toUser().getId())
			.orElseThrow(() -> new BusinessException("User not found", GlobalErrorCode.NOT_FOUND_ERROR));

		FriendRelationship friendRequest = FriendRelationship.builder()
			.fromUser(fromUser)
			.toUser(toUser)
			.status("PENDING")
			.build();

		return friendRelationshipRepository.save(friendRequest);
	}

	// 친구 요청 수락
	public void acceptFriendRequest(Long requestId) {
		FriendRelationship friendRequest = friendRelationshipRepository.findById(requestId)
			.orElseThrow(() -> new BusinessException("Friend request not found", GlobalErrorCode.NOT_FOUND_ERROR));

		friendRequest.setStatus("ACCEPTED");
		friendRelationshipRepository.save(friendRequest);
	}

	// 친구 요청 거절
	public void rejectFriendRequest(Long requestId) {
		FriendRelationship friendRequest = friendRelationshipRepository.findById(requestId)
			.orElseThrow(() -> new BusinessException("Friend request not found", GlobalErrorCode.NOT_FOUND_ERROR));

		friendRequest.setStatus("REJECTED");
		friendRelationshipRepository.save(friendRequest);
	}

	// 친구 삭제
	public void removeFriend(Long friendId) {
		if (!friendRelationshipRepository.existsById(friendId)) {
			throw new BusinessException("Friend not found", GlobalErrorCode.NOT_FOUND_ERROR);
		}
		friendRelationshipRepository.deleteById(friendId);
	}
}
