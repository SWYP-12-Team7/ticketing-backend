package com.example.ticketing.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteFolderRepository extends JpaRepository<FavoriteFolder, Long> {

    List<FavoriteFolder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<FavoriteFolder> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndName(Long userId, String name);

    int countByUserId(Long userId);
}