package com.adit.backend.domain.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adit.backend.domain.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
