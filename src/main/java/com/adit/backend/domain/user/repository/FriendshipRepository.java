package com.adit.backend.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adit.backend.domain.user.entity.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
	@Query("SELECT f1.toUser.id " +
		"FROM Friendship f1 " +
		"WHERE f1.fromUser.id = :userId AND f1.status = 'accept' " +
		"AND f1.toUser.id IN ( " +
		"    SELECT f2.fromUser.id " +
		"    FROM Friendship f2 " +
		"    WHERE f2.toUser.id = :userId AND f2.status = 'accept' " +
		")")
	Optional<List<Long>> findFriends(@Param("userId") Long userId);
}
