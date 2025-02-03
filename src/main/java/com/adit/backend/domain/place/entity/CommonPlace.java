package com.adit.backend.domain.place.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.dto.request.PlaceRequest;
import com.adit.backend.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonPlace extends BaseEntity {

	@Id
	private Long id;

	@Column(nullable = false)
	private String placeName;

	private BigDecimal latitude;
	private BigDecimal longitude;

	private String addressName;
	private String roadAddressName;
	private String subCategory;

	private String url;

	@Builder.Default
	@OneToMany(mappedBy = "commonPlace", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Image> images = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "commonPlace", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserPlace> userPlaces = new ArrayList<>();

	public void updatePlace(PlaceRequest requestDto) {
		this.placeName = requestDto.placeName();
		this.addressName = requestDto.addressName();
		this.latitude = requestDto.latitude();
		this.longitude = requestDto.longitude();
		this.roadAddressName = requestDto.roadAddressName();
		this.subCategory = requestDto.subCategory();
		this.url = requestDto.url();
	}

	//연관관계 메서드
	public void addUserPlace(UserPlace userPlace) {
		this.userPlaces.add(userPlace);
		userPlace.assignedCommonPlace(this);
	}

	public void addImage(Image image) {
		this.images.add(image);
		image.assignCommonPlace(this);
	}

}
