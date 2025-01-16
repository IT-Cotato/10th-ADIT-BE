package com.adit.backend.domain.event.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adit.backend.domain.event.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE DATE(e.startDate) = :date")
    List<Event> findByDate(@Param("date") LocalDate date);

    @Query("SELECT e FROM Event e WHERE e.startDate IS NULL AND e.endDate IS NULL")
    List<Event> findNoDateEvents();

    @Query("SELECT e FROM Event e ORDER BY e.visited DESC")
    List<Event> findPopularEvents();
}