package com.adit.backend.domain.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adit.backend.domain.user.entity.Friendship;
import com.adit.backend.domain.user.entity.User;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
	@Query("SELECT f1.toUser.id " +
		"FROM Friendship f1 " +
		"WHERE f1.fromUser.id = :userId AND f1.status = true " +
		"AND f1.toUser.id IN ( " +
		"    SELECT f2.fromUser.id " +
		"    FROM Friendship f2 " +
		"    WHERE f2.toUser.id = :userId AND f2.status = true " +
		")")
	List<Long> findFriends(@Param("userId") Long userId);

	@Query("SELECT fs FROM Friendship fs where fs.fromUser = :toUser AND fs.toUser = :fromUser")
	Friendship findByUser(@Param("fromUser") User fromUser, @Param("toUser") User toUser);


	@Modifying
	@Query("DELETE FROM Friendship fs where fs.fromUser.id = :userId AND fs.toUser.id = :friendId"
		+ " OR fs.fromUser.id = :friendId AND fs.toUser.id = :userId")
	void deleteFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

}
