package com.adit.backend.domain.image.entity;

import com.adit.backend.domain.event.entity.Event;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "common_place_id")
	private CommonPlace commonPlace;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_place_id")
	private UserPlace userPlace;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id")
	private Event event;

	@Column(nullable = false)
	private String url;

	@Builder
	public Image(Long id, CommonPlace commonPlace, UserPlace userPlace, Event event, String url) {
		this.id = id;
		this.commonPlace = commonPlace;
		this.userPlace = userPlace;
		this.event = event;
		this.url = url;
	}

	//연관관계 메서드
	public void assignEvent(Event event) {
		this.event = event;
	}

	public void assignCommonPlace(CommonPlace commonPlace) {
		this.commonPlace = commonPlace;
	}

	public void assignUserPlace(UserPlace userPlace) {
		this.userPlace = userPlace;
	}
}