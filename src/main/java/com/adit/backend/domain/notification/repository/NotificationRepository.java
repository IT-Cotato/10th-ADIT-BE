package com.adit.backend.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adit.backend.domain.notification.entity.Notification;
import com.adit.backend.domain.user.entity.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findAllByUser(User user);
}