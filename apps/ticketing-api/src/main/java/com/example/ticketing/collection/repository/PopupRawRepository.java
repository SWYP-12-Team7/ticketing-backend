package com.example.ticketing.collection.repository;

import com.example.ticketing.collection.domain.PopupRaw;
import com.example.ticketing.collection.domain.ReviewStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PopupRawRepository extends JpaRepository<PopupRaw, Long> {

    Optional<PopupRaw> findByPopupId(String popupId);

    boolean existsByPopupId(String popupId);

    boolean existsByTitle(String title);

    List<PopupRaw> findByReviewStatus(ReviewStatus reviewStatus);

    @Query("SELECT p.popupId FROM PopupRaw p WHERE p.popupId LIKE 'popga-%'")
    Set<String> findAllPopgaPopupIds();
}
