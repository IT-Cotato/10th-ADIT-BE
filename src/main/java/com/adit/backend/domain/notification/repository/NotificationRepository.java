package com.adit.backend.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adit.backend.domain.notification.entity.Notification;
import com.adit.backend.domain.user.entity.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findAllByUser(User user);

	Optional<List<Notification>> findAllByUserAndIdGreaterThan(User user, Long lastId);

	@Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt >= :cutoffDate ORDER BY n.createdAt ASC")
	Optional<List<Notification>> findRecentNotifications(@Param("user") User user,
		@Param("cutoffDate") LocalDateTime cutoffDate);
}