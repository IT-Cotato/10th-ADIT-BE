package com.adit.backend.domain.image.converter;

import org.springframework.stereotype.Component;

import com.adit.backend.domain.image.dto.response.ImageResponseDto;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.dto.request.PlaceRequestDto;

@Component
public class ImageConverter {
	public Image toEntity(PlaceRequestDto request) {
		return Image.builder()
			.url(request.url())
			.build();
	}

	public ImageResponseDto toResponse(Image image) {
		return ImageResponseDto.builder()
			.id(image.getId())
			.commonPlace(image.getCommonPlace())
			.userPlace(image.getUserPlace())
			.userEvent(image.getUserEvent())
			.commonEvent(image.getCommonEvent())
			.url(image.getUrl())
			.build();
	}
}
