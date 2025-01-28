package com.adit.backend.domain.user.entity;

import com.adit.backend.global.entity.BaseEntity;

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
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_user_id", nullable = false)
	private User fromUser;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_user_id", nullable = false)
	private User toUser;

	private Boolean status;

	@Builder
	public Friendship(Long id, User fromUser, User toUser, Boolean status) {
		this.id = id;
		this.fromUser = fromUser;
		this.toUser = toUser;
		this.status = status;
	}

	public void acceptRequest(){
		this.status = true;
	}
}
