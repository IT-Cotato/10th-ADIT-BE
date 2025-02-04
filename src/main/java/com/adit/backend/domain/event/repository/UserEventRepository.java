package com.adit.backend.domain.event.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adit.backend.domain.event.entity.UserEvent;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

	@Query("SELECT e FROM UserEvent e WHERE DATE(e.startDate) = :date")
	List<UserEvent> findByDate(@Param("date") LocalDate date);

	@Query("SELECT e FROM UserEvent e WHERE e.startDate IS NULL AND e.endDate IS NULL")
	List<UserEvent> findNoDateEvents();

	@Query("SELECT e FROM UserEvent e ORDER BY e.visited DESC")
	List<UserEvent> findPopularEvents();
}