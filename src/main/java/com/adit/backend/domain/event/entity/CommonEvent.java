package com.adit.backend.domain.event.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.adit.backend.domain.event.dto.request.EventUpdateRequestDto;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class CommonEvent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String name;

	@Column(nullable = false)
	private String category;

	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String memo;

	@Builder.Default
	@OneToMany(mappedBy = "commonEvent", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Image> images = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "commonEvent", cascade = CascadeType.ALL, orphanRemoval = true)
	List<UserEvent> userEvents = new ArrayList<>();

	public static CommonEvent createEvent(String name, String category, LocalDateTime startDate, LocalDateTime endDate,
		String memo) {
		CommonEvent commonEvent = new CommonEvent();
		commonEvent.name = name;
		commonEvent.category = category;
		commonEvent.startDate = startDate;
		commonEvent.endDate = endDate;
		commonEvent.memo = memo;
		return commonEvent;
	}

	//연관관계 메서드
	public void addUserEvent(UserEvent userEvent) {
		this.userEvents.add(userEvent);
		userEvent.assignCommonEvent(this);
	}

	public void addImage(Image image) {
		this.images.add(image);
		image.assignCommonEvent(this);
	}

	public void updateEvent(EventUpdateRequestDto updateRequest) {
		if (updateRequest.getName() != null)
			this.name = updateRequest.getName();
		if (updateRequest.getCategory() != null)
			this.category = updateRequest.getCategory();
		if (updateRequest.getStartDate() != null)
			this.startDate = updateRequest.getStartDate();
		if (updateRequest.getEndDate() != null)
			this.endDate = updateRequest.getEndDate();
		if (updateRequest.getMemo() != null)
			this.memo = updateRequest.getMemo();
	}
}