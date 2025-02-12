package com.adit.backend.domain.notification.entity;

import org.hibernate.annotations.ColumnDefault;

import com.adit.backend.domain.notification.enums.NotificationType;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.global.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private String message;

	@NotNull
	@Enumerated(EnumType.STRING)
	private NotificationType notificationType;

	@NotNull
	private String category;

	@NotNull
	@ColumnDefault("false")
	private boolean isRead = false;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Builder
	public Notification(String message, NotificationType notificationType, String category) {
		this.message = message;
		this.notificationType = notificationType;
		this.category = category;
	}

	//연관관계 메서드
	public void assignUser(User user) {
		this.user = user;
	}

	public void markAsRead() {
		this.isRead = true;
	}
}
