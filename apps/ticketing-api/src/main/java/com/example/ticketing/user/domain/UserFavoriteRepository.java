package com.example.ticketing.user.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    Optional<UserFavorite> findByUserIdAndCurationId(Long userId, Long curationId);

    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndCurationId(Long userId, Long curationId);

    void deleteByUserIdAndCurationId(Long userId, Long curationId);
}