package com.adit.backend.domain.place.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;
import static com.adit.backend.global.util.MapUtil.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.place.converter.CommonPlaceConverter;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.domain.place.exception.PlaceException;
import com.adit.backend.domain.place.repository.UserPlaceRepository;
import com.adit.backend.domain.user.converter.UserConverter;
import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.exception.UserException;
import com.adit.backend.domain.user.repository.FriendshipRepository;
import com.adit.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserPlaceQueryService {

	private final UserPlaceRepository userPlaceRepository;
	private final FriendshipRepository friendshipRepository;
	private final CommonPlaceConverter commonPlaceConverter;
	private final UserRepository userRepository;
	private final UserConverter userConverter;

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

		return userPlaces.stream().map(commonPlaceConverter::userPlaceToResponse).toList();
	}

	public List<PlaceResponseDto> getSavedPlace(Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByUserId(userId);
		if (userPlaces.isEmpty()) {
			throw new PlaceException(USER_PLACE_NOT_FOUND);
		}
		return userPlaces.stream().map(commonPlaceConverter::userPlaceToResponse).toList();
	}

	public List<PlaceResponseDto> getPlaceByLocation(double userLatitude, double userLongitude, Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByUserId(userId);
		if (userPlaces.isEmpty()) {
			throw new PlaceException(USER_PLACE_NOT_FOUND);
		}
		if (userPlaces.size() == 1) {
			return userPlaces.stream().map(commonPlaceConverter::userPlaceToResponse).toList();
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
		return placeByLocation.stream().map(commonPlaceConverter::userPlaceToResponse).toList();
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
		return userPlaces.stream().map(commonPlaceConverter::userPlaceToResponse).toList();
	}

	public Map<PlaceResponseDto, List<UserResponse.InfoDto>> getPlaceByFriend(Long userId) {
		// 기존: throw new FriendNotFoundException("Friend not found");
		Map<PlaceResponseDto, List<UserResponse.InfoDto>> response = new LinkedHashMap<>();
		List<Long> friendsId = friendshipRepository.findFriends(userId);
		if (friendsId.isEmpty()) {
			throw new PlaceException(FRIEND_NOT_FOUND);
		}
		Map<CommonPlace, Integer> friendsCommonplace = new HashMap<>();
		friendsId.forEach(id -> {
			 userPlaceRepository.findByUserId(id)
				 .forEach(userPlace -> friendsCommonplace.merge(userPlace.getCommonPlace(), 1, Integer::sum));

		});
		Set<CommonPlace> key = friendsCommonplace.keySet();
		List<CommonPlace> keyList = new ArrayList<>(key);
		keyList.sort((a1,b1) -> friendsCommonplace.get(b1) - friendsCommonplace.get(a1));
		for(CommonPlace commonPlace : keyList){
			List<Long> friendIds = userPlaceRepository.findByCommonPlaceId(commonPlace.getId());
			List<UserResponse.InfoDto> friendinfoList = friendIds.stream()
				.map(id -> userRepository.findById(id).orElseThrow(() -> new UserException(USER_NOT_FOUND)))
				.map(userConverter::InfoDto)
				.toList();

			response.put(commonPlaceConverter.commonPlaceToResponse(commonPlace), friendinfoList);
		}
		return response;
	}
}
