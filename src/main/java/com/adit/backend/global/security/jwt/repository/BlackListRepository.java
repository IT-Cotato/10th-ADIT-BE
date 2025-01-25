package com.adit.backend.global.security.jwt.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.adit.backend.global.security.jwt.entity.BlackList;

@Repository
public interface BlackListRepository extends CrudRepository<BlackList, String> {
}