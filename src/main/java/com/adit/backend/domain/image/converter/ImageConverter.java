package com.adit.backend.domain.image.converter;

import org.springframework.stereotype.Component;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.dto.request.PlaceRequest;

@Component
public class ImageConverter {
	public Image toEntity(PlaceRequest request) {
		return Image.builder()
			.url(request.url())
			.build();
	}
}
