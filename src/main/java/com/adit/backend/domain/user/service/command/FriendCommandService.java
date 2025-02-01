package com.adit.backend.domain.user.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.user.converter.FriendConverter;
import com.adit.backend.domain.user.dto.request.FriendRequestDto;
import com.adit.backend.domain.user.dto.response.FriendshipResponseDto;
import com.adit.backend.domain.user.entity.Friendship;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.exception.FriendShipException;
import com.adit.backend.domain.user.repository.FriendshipRepository;
import com.adit.backend.domain.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class FriendCommandService {

	private final UserRepository userRepository;
	private final FriendshipRepository friendshipRepository;
	private final FriendConverter friendConverter;

	// 친구 요청 보내기
	public FriendshipResponseDto sendFriendRequest(FriendRequestDto requestDto) {
		// 친구 요청(정방향)
		Friendship forwardRequest = friendConverter.toForwardEntity(requestDto);
		// 친구 요청(역방향)
		Friendship reverseRequest = friendConverter.toReverseEntity(requestDto);

		Friendship savedForwardRequest = friendshipRepository.save(forwardRequest);
		friendshipRepository.save(reverseRequest);

		return friendConverter.toResponse(savedForwardRequest);
	}

	// 친구 요청 수락
	public void acceptFriendRequest(Long requestId) {
		Friendship friendRequest = friendshipRepository.findById(requestId)
			.orElseThrow(() -> new FriendShipException(FRIEND_REQUEST_NOT_FOUND));

		friendRequest.acceptRequest();
	}

	// 친구 요청 거절
	public void rejectFriendRequest(Long requestId) {
		Friendship friendRequest = friendshipRepository.findById(requestId)
			.orElseThrow(() -> new FriendShipException(FRIEND_REQUEST_NOT_FOUND));

		User fromUser = friendRequest.getFromUser();
		User toUser = friendRequest.getToUser();

		Friendship friendship = friendshipRepository.findByUser(fromUser, toUser);

		friendshipRepository.deleteById(requestId);
		friendshipRepository.delete(friendship);
	}

	// 친구 삭제
	public void removeFriend(Long userId, Long friendId) {
		List<Long> friends = friendshipRepository.findFriends(userId);
		if (!friends.contains(friendId)) {
			throw new FriendShipException(FRIEND_NOT_FOUND);
		}
		friendshipRepository.deleteFriend(userId, friendId);
	}
}
