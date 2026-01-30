package com.example.ticketing.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCategoryPreferenceRepository extends JpaRepository<UserCategoryPreference, Long> {
    List<UserCategoryPreference> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
