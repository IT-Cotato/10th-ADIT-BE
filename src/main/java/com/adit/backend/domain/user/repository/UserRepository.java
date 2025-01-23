package com.adit.backend.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.adit.backend.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findBySocialId(String socialId);

	boolean existsByNickname(String nickname);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.token t WHERE t.accessToken = ?1")
	Optional<User> findUserByToken_AccessToken(String accessToken);

	Optional<User> findByNickname(String nickName);
}
