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
import com.adit.backend.domain.user.exception.user.UserNotFoundException;
import com.adit.backend.domain.user.repository.FriendshipRepository;
import com.adit.backend.domain.user.repository.UserRepository;

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
	private final UserConverter userConverter;

	//친구 요청 보내기
	public FriendshipResponseDto sendFriendRequest(FriendRequestDto requestDto) {
		User fromUser = userRepository.findById(requestDto.fromUser().getId())
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		User toUser = userRepository.findById(requestDto.toUser().getId())
			.orElseThrow(() -> new UserNotFoundException("User not found"));

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

	// 친구 요청 거절
	public void rejectFriendRequest(Long requestId) {
		Friendship friendRequest = friendshipRepository.findById(requestId)
			.orElseThrow(() -> new FriendRequestNotFoundException("Friend request not found"));

		User fromUser = friendRequest.getFromUser();
		User toUser = friendRequest.getToUser();
		Friendship friendship = friendshipRepository.findByUser(fromUser, toUser);

		friendshipRepository.deleteById(requestId);
		friendshipRepository.delete(friendship);

	}

	// 친구 삭제
	public void removeFriend(Long userId, Long friendId) {
		List<Long> friends = friendshipRepository.findFriends(userId);
		if (!friends.contains(friendId)){
			throw new UserNotFoundException("Friend not found");
		}
		friendshipRepository.deleteFriend(userId, friendId);
	}

	//친구 요청 목록 확인
	public Map<String, List<FriendshipResponseDto>> checkRequest(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
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
				.orElseThrow(() -> new UserNotFoundException("User not found")))
			.map(userConverter::InfoDto).toList();

	}

	//사용자 검색
	public Map<String, UserResponse.InfoDto> findUser(String nickName, Long userId) {
		User searchedUser = userRepository.findByNickname(nickName)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

		List<Long> friendsId = friendshipRepository.findFriends(userId);
		Friendship byUser = friendshipRepository.findByUser(user, searchedUser);

		Map<String, UserResponse.InfoDto> response = new HashMap<>();
		//nickName 으로 검색된 사용자가 친구 요청 대기중이거나 이미 친구라면 메시지만 반환
		if (friendsId.contains(searchedUser.getId()) || byUser != null) {
			response.put("요청 대기중 이거나 이미 친구인 사용자 입니다", null);
			return response;
		}
		// 그런 경우가 아니라면 검색된 사용자 정보 반환
		else {
			response.put("", userConverter.InfoDto(searchedUser));
			return response;
		}
	}
}
