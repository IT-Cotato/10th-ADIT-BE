package com.adit.backend.domain.user.converter;

import org.springframework.stereotype.Component;

import com.adit.backend.domain.user.dto.request.FriendRequestDto;
import com.adit.backend.domain.user.dto.response.FriendshipResponseDto;
import com.adit.backend.domain.user.entity.Friendship;

@Component
public class FriendConverter {

	public FriendshipResponseDto toResponse(Friendship friendship) {
		return FriendshipResponseDto.builder()
			.fromUser(friendship.getFromUser())
			.toUser(friendship.getToUser())
			.status(friendship.getStatus())
			.build();
	}

	public Friendship toForwardEntity(FriendRequestDto friendRequestDto) {
		return Friendship.builder()
			.fromUser(friendRequestDto.fromUser())
			.toUser(friendRequestDto.toUser())
			.status(true)
			.build();
	}

	public Friendship toReverseEntity(FriendRequestDto friendRequestDto) {
		return Friendship.builder()
			.fromUser(friendRequestDto.toUser())
			.toUser(friendRequestDto.fromUser())
			.status(false)
			.build();
	}
}
