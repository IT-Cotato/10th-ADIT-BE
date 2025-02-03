package com.adit.backend.domain.place.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;
import static com.adit.backend.global.util.MapUtil.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.place.converter.PlaceConverter;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.domain.place.exception.PlaceException;
import com.adit.backend.domain.place.repository.UserPlaceRepository;
import com.adit.backend.domain.user.repository.FriendshipRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserPlaceQueryService {

	private final UserPlaceRepository userPlaceRepository;
	private final FriendshipRepository friendshipRepository;
	private final PlaceConverter placeConverter;

	public List<PlaceResponseDto> getPlaceByCategory(List<String> subCategory, Long userId) {
		// 기존: throw new NotValidException("RequestParam not valid");
		if (subCategory.stream().anyMatch(String::isBlank)) {
			throw new PlaceException(NOT_VALID);
		}

		// 중복 제거를 위한 Set
		Set<UserPlace> userPlaceSet = new HashSet<>();
		subCategory.forEach(partialCategory -> {
			List<UserPlace> foundPlaces = userPlaceRepository.findByCategory(partialCategory, userId);
			userPlaceSet.addAll(foundPlaces);
		});
		List<UserPlace> userPlaces = new ArrayList<>(userPlaceSet);
		// 기존: throw new UserPlaceNotFoundException("UserPlace not found");
		if (userPlaces.isEmpty()) {
			throw new PlaceException(USER_PLACE_NOT_FOUND);
		}

		return userPlaces.stream().map(placeConverter::userPlaceToResponse).toList();
	}

	public List<PlaceResponseDto> getSavedPlace(Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByUserId(userId);
		if (userPlaces.isEmpty()) {
			throw new PlaceException(USER_PLACE_NOT_FOUND);
		}
		return userPlaces.stream().map(placeConverter::userPlaceToResponse).toList();
	}

	public List<PlaceResponseDto> getPlaceByLocation(double userLatitude, double userLongitude, Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByUserId(userId);
		if (userPlaces.isEmpty()) {
			throw new PlaceException(USER_PLACE_NOT_FOUND);
		}
		if (userPlaces.size() == 1) {
			return userPlaces.stream().map(placeConverter::userPlaceToResponse).toList();
		}
		// 저장한 장소가 2개 이상일 때 정렬
		List<UserPlace> placeByLocation = userPlaces.stream()
			.sorted((place1, place2) -> {
				double distance1 = getDistance(
					place1.getCommonPlace().getLatitude().doubleValue(),
					place1.getCommonPlace().getLongitude().doubleValue(),
					userLatitude, userLongitude
				);
				double distance2 = getDistance(
					place2.getCommonPlace().getLatitude().doubleValue(),
					place2.getCommonPlace().getLongitude().doubleValue(),
					userLatitude, userLongitude
				);
				return Double.compare(distance1, distance2);
			})
			.toList();
		return placeByLocation.stream().map(placeConverter::userPlaceToResponse).toList();
	}

	public List<PlaceResponseDto> getPlaceByAddress(List<String> address, Long userId) {
		if (address.stream().anyMatch(String::isBlank)) {
			throw new PlaceException(NOT_VALID);
		}
		Set<UserPlace> userPlaceSet = new HashSet<>();
		address.forEach(partialAddress -> {
			List<UserPlace> foundPlaces = userPlaceRepository.findByAddress(partialAddress, userId);
			userPlaceSet.addAll(foundPlaces);
		});
		List<UserPlace> userPlaces = new ArrayList<>(userPlaceSet);
		if (userPlaces.isEmpty()) {
			throw new PlaceException(USER_PLACE_NOT_FOUND);
		}
		return userPlaces.stream().map(placeConverter::userPlaceToResponse).toList();
	}

	public List<PlaceResponseDto> getPlaceByFriend(Long userId) {
		// 기존: throw new FriendNotFoundException("Friend not found");
		List<Long> friendsId = friendshipRepository.findFriends(userId);
		if (friendsId.isEmpty()) {
			throw new PlaceException(FRIEND_NOT_FOUND);
		}
		Set<UserPlace> friendsCommonplaceSet = new HashSet<>();
		friendsId.forEach(id -> {
			List<UserPlace> foundPlaces = userPlaceRepository.findByUserId(id);
			friendsCommonplaceSet.addAll(foundPlaces);
		});
		List<UserPlace> friendsCommonplace = new ArrayList<>(friendsCommonplaceSet);
		return friendsCommonplace.stream().map(placeConverter::friendToResponse).toList();
	}
}
