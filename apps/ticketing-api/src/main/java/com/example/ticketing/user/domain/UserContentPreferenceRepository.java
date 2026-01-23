package com.example.ticketing.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserContentPreferenceRepository extends JpaRepository<UserContentPreference, Long> {
    List<UserContentPreference> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndContentId(Long userId, Long contentId);
}
