package com.adit.backend.domain.place.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.PlaceStatistics;

@Repository
public interface PlaceStatisticsRepository extends JpaRepository<PlaceStatistics, Long> {

	@Query("SELECT ps FROM PlaceStatistics ps where ps.commonPlace.id = :id")
	Optional<PlaceStatistics> findByCommonPlaceId(@Param("id") Long id);


}
