package com.adit.backend.domain.image.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adit.backend.domain.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

	@Query("SELECT im FROM Image im WHERE im.userPlace.id = :id")
	Optional<Image> findByUserPlaceId(@Param("id") Long userPlaceId);
}
