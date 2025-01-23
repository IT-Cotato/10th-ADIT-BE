package com.adit.backend.domain.user.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.user.converter.FriendConverter;
import com.adit.backend.domain.user.converter.UserConverter;
import com.adit.backend.domain.user.dto.request.FriendRequestDto;
import com.adit.backend.domain.user.dto.response.FriendshipResponseDto;
import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.entity.Friendship;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.exception.friend.FriendRequestNotFoundException;
import com.adit.backend.domain.user.exception.user.UserException;
import com.adit.backend.domain.user.repository.FriendshipRepository;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.global.error.GlobalErrorCode;

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
	private final FriendConverter friendConverter;

	//친구 요청 보내기
	public FriendshipResponseDto sendFriendRequest(FriendRequestDto requestDto) {
		User fromUser = userRepository.findById(requestDto.fromUser().getId())
			.orElseThrow(() -> new UserException(GlobalErrorCode.NOT_FOUND_ERROR));
		User toUser = userRepository.findById(requestDto.toUser().getId())
			.orElseThrow(() -> new UserException(GlobalErrorCode.NOT_FOUND_ERROR));

		Friendship forwardRequest = friendConverter.toForwardEntity(requestDto);

		Friendship reverseRequest = friendConverter.toReverseEntity(requestDto);

		Friendship savedForwardRequest = friendshipRepository.save(forwardRequest);
		friendshipRepository.save(reverseRequest);
		return friendConverter.toResponse(savedForwardRequest);
	}

	// 친구 요청 수락
	public void acceptFriendRequest(Long requestId) {
		Friendship friendRequest = friendshipRepository.findById(requestId)
			.orElseThrow(() -> new FriendRequestNotFoundException("Friend request not found"));

		friendRequest.acceptRequest();
	}
	//
	// // 친구 요청 거절
	// public void rejectFriendRequest(Long requestId) {
	// 	Friendship friendRequest = friendshipRepository.findById(requestId)
	// 		.orElseThrow(() -> new BusinessException("Friend request not found", GlobalErrorCode.NOT_FOUND_ERROR));
	//
	// 	friendRequest.setStatus("REJECTED");
	// 	friendshipRepository.save(friendRequest);
	// }
	//
	// // 친구 삭제
	// public void removeFriend(Long friendId) {
	// 	if (!friendshipRepository.existsById(friendId)) {
	// 		throw new BusinessException("Friend not found", GlobalErrorCode.NOT_FOUND_ERROR);
	// 	}
	// 	friendshipRepository.deleteById(friendId);
	// }

	//친구 요청 목록 확인
	public Map<String, List<FriendshipResponseDto>> checkRequest(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserException(GlobalErrorCode.USER_NOT_FOUND));
		List<Friendship> sentFriendRequests = user.getSentFriendRequests();
		List<Friendship> receivedFriendRequests = user.getReceivedFriendRequests();

		Map<String, List<FriendshipResponseDto>> allRequests = new HashMap<>();
		allRequests.put("sentRequests", sentFriendRequests.stream().map(friendConverter::toResponse).toList());
		allRequests.put("receivedRequests", receivedFriendRequests.stream().map(friendConverter::toResponse).toList());
		return allRequests;
	}

	//친구 목록 확인
	public List<UserResponse.InfoDto> findFriends(Long userId){
		List<Long> friendsId = friendshipRepository.findFriends(userId);
		return friendsId.stream().map(id -> userRepository.findById(id)
				.orElseThrow(() -> new UserException(GlobalErrorCode.USER_NOT_FOUND)))
			.map(UserConverter::InfoDto).toList();

	}
}
