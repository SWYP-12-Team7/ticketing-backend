package com.example.ticketing.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPreferredRegionRepository extends JpaRepository<UserPreferredRegion, Long> {
    List<UserPreferredRegion> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
