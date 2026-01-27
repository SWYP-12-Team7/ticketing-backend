package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.domain.PopupLike;
import com.example.ticketing.user.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PopupLikeRepository extends JpaRepository<PopupLike, Long> {

    Optional<PopupLike> findByUserAndPopup(User user, Popup popup);

    boolean existsByUserIdAndPopupId(Long userId, Long popupId);

    @Query("SELECT pl.popup.id FROM PopupLike pl WHERE pl.user.id = :userId AND pl.popup.id IN :popupIds")
    Set<Long> findLikedPopupIdsByUserIdAndPopupIds(@Param("userId") Long userId, @Param("popupIds") List<Long> popupIds);

    @Query("SELECT pl.popup.id FROM PopupLike pl WHERE pl.user.id = :userId")
    Set<Long> findLikedPopupIdsByUserId(@Param("userId") Long userId);
}