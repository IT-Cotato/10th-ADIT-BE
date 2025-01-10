package com.adit.backend.domain.place.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adit.backend.domain.place.entity.CommonPlace;

public interface CommonPlaceRepository extends JpaRepository<CommonPlace, Long> {
	@Query("SELECT c FROM CommonPlace c WHERE c.subCategory = :subCategory")
	Optional<List<CommonPlace>> findByCategory(@Param("subCategory") String subCategory);

	@Query("SELECT ps.commonPlace.id FROM PlaceStatistics ps ORDER BY ps.bookmarkCount DESC")
	Optional<List<Long>> findByPopular(Pageable pageable);

}