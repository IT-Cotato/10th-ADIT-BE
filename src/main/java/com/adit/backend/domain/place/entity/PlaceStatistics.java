package com.adit.backend.domain.place.entity;

import com.adit.backend.global.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceStatistics extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "common_place_id", nullable = false)
	private CommonPlace commonPlace;

	private int bookmarkCount;
	private Integer visitCount;

	@Builder
	public PlaceStatistics(CommonPlace commonPlace, int bookmarkCount, Integer visitCount){
		this.commonPlace = commonPlace;
		this.bookmarkCount = bookmarkCount;
		this.visitCount = visitCount;
	}

	public void updateBookMarkCount(){
		this.bookmarkCount += 1;
	}
}
