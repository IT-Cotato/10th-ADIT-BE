package com.adit.backend.domain.user.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.user.converter.UserConverter;
import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.entity.Friendship;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.exception.UserException;
import com.adit.backend.domain.user.repository.FriendshipRepository;
import com.adit.backend.domain.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class FriendQueryService {

	private final UserRepository userRepository;
	private final FriendshipRepository friendshipRepository;
	private final UserConverter userConverter;

	/**
	 * 친구 요청 목록 확인
	 */
	public Map<String, List<UserResponse.InfoDto>> checkRequest(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserException(USER_NOT_FOUND));
		List<Friendship> sentFriendRequests = user.getSentFriendRequests();
		List<Friendship> receivedFriendRequests = user.getReceivedFriendRequests();

		Map<String, List<UserResponse.InfoDto>> allRequests = new HashMap<>();

		// 사용자의 보낸 요청
		allRequests.put("sentRequests", sentFriendRequests.stream()
			.map(friendship -> friendshipRepository.findByUser(friendship.getFromUser(), friendship.getToUser()))
			.filter(friendship -> !friendship.getStatus())
			.map(friendship -> userRepository.findById(friendship.getFromUser().getId())
				.orElseThrow(() -> new UserException(USER_NOT_FOUND)))
			.map(userConverter::InfoDto)
			.toList());

		// 사용자의 받은 요청
		allRequests.put("receivedRequests", receivedFriendRequests.stream()
			.map(friendship -> friendshipRepository.findByUser(friendship.getFromUser(), friendship.getToUser()))
			.filter(friendship -> !friendship.getStatus())
			.map(friendship -> userRepository.findById(friendship.getToUser().getId())
				.orElseThrow(() -> new UserException(USER_NOT_FOUND)))
			.map(userConverter::InfoDto)
			.toList());

		return allRequests;
	}

	/**
	 * 친구 목록 확인
	 */
	public List<UserResponse.InfoDto> findFriends(Long userId) {
		List<Long> friendsId = friendshipRepository.findFriends(userId);
		return friendsId.stream()
			.map(id -> userRepository.findById(id)
				.orElseThrow(() -> new UserException(USER_NOT_FOUND)))
			.map(userConverter::InfoDto)
			.toList();
	}

	/**
	 * 사용자 검색
	 */
	public Map<String, UserResponse.InfoDto> findUser(String nickName, Long userId) {
		User searchedUser = userRepository.findByNickname(nickName)
			.orElseThrow(() -> new UserException(USER_NOT_FOUND));
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserException(USER_NOT_FOUND));

		List<Long> friendsId = friendshipRepository.findFriends(userId);
		Friendship byUser = friendshipRepository.findByUser(user, searchedUser);

		Map<String, UserResponse.InfoDto> response = new HashMap<>();
		// nickName 으로 검색된 사용자가 친구 요청 대기중이거나 이미 친구라면 메시지만 반환
		if (friendsId.contains(searchedUser.getId()) || byUser != null) {
			response.put("요청 대기중 이거나 이미 친구인 사용자 입니다", null);
			return response;
		}
		// 그렇지 않다면 검색된 사용자 정보 반환
		else {
			response.put("", userConverter.InfoDto(searchedUser));
			return response;
		}
	}

}
