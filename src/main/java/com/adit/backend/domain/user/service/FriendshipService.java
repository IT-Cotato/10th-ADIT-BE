package com.adit.backend.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.user.dto.request.FriendRequestDto;
import com.adit.backend.domain.user.entity.Friendship;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.repository.FriendshipRepository;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendshipService {

	private final FriendshipRepository friendshipRepository;
	private final UserRepository userRepository;

	// 친구 요청 보내기

	public Friendship sendFriendRequest(FriendRequestDto requestDto) {
		User fromUser = userRepository.findById(requestDto.fromUser().getId())
			.orElseThrow(() -> new BusinessException("User not found", GlobalErrorCode.NOT_FOUND_ERROR));
		User toUser = userRepository.findById(requestDto.toUser().getId())
			.orElseThrow(() -> new BusinessException("User not found", GlobalErrorCode.NOT_FOUND_ERROR));

		Friendship friendRequest = Friendship.builder()
			.fromUser(fromUser)
			.toUser(toUser)
			.status("PENDING")
			.build();

		return friendshipRepository.save(friendRequest);
	}

	// 친구 요청 수락
	public void acceptFriendRequest(Long requestId) {
		Friendship friendRequest = friendshipRepository.findById(requestId)
			.orElseThrow(() -> new BusinessException("Friend request not found", GlobalErrorCode.NOT_FOUND_ERROR));

		friendRequest.setStatus("ACCEPTED");
		friendshipRepository.save(friendRequest);
	}

	// 친구 요청 거절
	public void rejectFriendRequest(Long requestId) {
		Friendship friendRequest = friendshipRepository.findById(requestId)
			.orElseThrow(() -> new BusinessException("Friend request not found", GlobalErrorCode.NOT_FOUND_ERROR));

		friendRequest.setStatus("REJECTED");
		friendshipRepository.save(friendRequest);
	}

	// 친구 삭제
	public void removeFriend(Long friendId) {
		if (!friendshipRepository.existsById(friendId)) {
			throw new BusinessException("Friend not found", GlobalErrorCode.NOT_FOUND_ERROR);
		}
		friendshipRepository.deleteById(friendId);
	}
}
