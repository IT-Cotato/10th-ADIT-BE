package com.adit.backend.domain.event.entity;

import com.adit.backend.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@Entity
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventStatistics extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "common_event_id", nullable = false)
	private CommonEvent commonEvent;

	@Column(nullable = false)
	private Integer bookmarkCount = 0; // 기본값 추가

	@Column(nullable = false)
	private Integer visitCount = 0; // 기본값 추가

	// bookmarkCount 증가 메서드 추가
	public void incrementBookmarkCount() {
		this.bookmarkCount++;
	}

	// visitCount 증가 메서드 추가
	public void incrementVisitCount() {
		this.visitCount++;
	}
}