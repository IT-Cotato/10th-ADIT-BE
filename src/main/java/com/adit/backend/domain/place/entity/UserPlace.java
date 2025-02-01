package com.adit.backend.domain.place.entity;

import java.util.List;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPlace extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "common_place_id", nullable = false)
	private CommonPlace commonPlace;

	@OneToMany(mappedBy = "userPlace", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Image> images;

	private String memo;
	private Boolean visited;

	@Builder
	public UserPlace(User user, CommonPlace commonPlace, String memo, Boolean visited) {
		this.user = user;
		this.commonPlace = commonPlace;
		this.memo = memo;
		this.visited = visited;
	}

	public void updatedMemo(String memo) {
		this.memo = memo;
	}

	public void updatedVisited() {
		this.visited = true;
	}

	//연관관계 메서드
	public void addImage(Image image) {
		this.images.add(image);
		image.assignUserPlace(this);
	}

	public void assignedCommonPlace(CommonPlace commonPlace) {
		this.commonPlace = commonPlace;
	}

	public void assignedUser(User user) {
		this.user = user;
	}
}
