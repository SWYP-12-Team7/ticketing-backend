package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.Popup;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long> {

    @Query("SELECT p FROM Popup p WHERE " +
           "(:keyword IS NULL OR p.title LIKE %:keyword%) AND " +
           "(:region IS NULL OR p.region = :region)")
    Page<Popup> findByFilters(
        @Param("keyword") String keyword,
        @Param("region") String region,
        Pageable pageable
    );

    Optional<Popup> findById(Long id);
}
