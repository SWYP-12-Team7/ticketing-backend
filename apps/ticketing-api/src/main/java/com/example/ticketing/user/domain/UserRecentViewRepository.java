package com.example.ticketing.user.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRecentViewRepository extends JpaRepository<UserRecentView, Long> {

    Optional<UserRecentView> findByUserIdAndCurationId(Long userId, Long curationId);

    List<UserRecentView> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    void deleteByUserId(Long userId);
}