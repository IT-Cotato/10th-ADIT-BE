package com.adit.backend.domain.event.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adit.backend.domain.event.entity.CommonEvent;

public interface CommonEventRepository extends JpaRepository<CommonEvent, Long> {
  Optional<CommonEvent> findByName(String name);
}