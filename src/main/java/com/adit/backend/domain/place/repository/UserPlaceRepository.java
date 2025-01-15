package com.adit.backend.domain.place.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;

@Repository
public interface UserPlaceRepository extends JpaRepository<UserPlace, Long> {
	@Query("SELECT up FROM UserPlace up WHERE up.user.id = :userId AND up.commonPlace.subCategory = :subCategory")
	List<UserPlace> findByCategory(@Param("subCategory") String subCategory,@Param("userId") Long userId);

	@Query("SELECT up FROM UserPlace up where up.user.id = :userId")
	List<UserPlace> findByUserId(@Param("userId") Long userId);

	@Query("SELECT up FROM UserPlace up where up.commonPlace.addressName LIKE %:partialAddress% AND up.user.id = :userId")
	List<UserPlace> findByAddress(@Param("partialAddress") String partialAddress, @Param("userId") Long userId);

}
