package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.Popup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long> {

    Page<Popup> findAll(Pageable pageable);

    @Query("SELECT p FROM Popup p WHERE " +
           "(:keyword IS NULL OR p.title LIKE %:keyword%) AND " +
           "(:city IS NULL OR p.city = :city)")
    Page<Popup> findByFilters(
        @Param("keyword") String keyword,
        @Param("city") String city,
        Pageable pageable
    );

    Optional<Popup> findByPopupId(String popupId);

    /**
     * 무료 행사 조회
     * 정렬: 제목 오름차순
     */
    @Query("SELECT p FROM Popup p WHERE p.isFree = true ORDER BY p.title ASC")
    List<Popup> findFreePopups();
}
