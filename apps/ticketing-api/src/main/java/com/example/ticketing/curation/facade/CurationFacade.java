package com.example.ticketing.curation.facade;

import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.user.domain.UserFavorite;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CurationFacade {

    private final UserFavoriteRepository userFavoriteRepository;

    public void toggle(Long userId, Long curationId, CurationType type) {

        Optional<UserFavorite> userFavorite = userFavoriteRepository.findByUserIdAndCurationId(userId, curationId);

        if (userFavorite.isPresent()) {
            // 이미 찜함 -> 찜 취소
            userFavoriteRepository.delete(userFavorite.get());
        } else {
            // 아직 안함 -> 찜 추가
            userFavoriteRepository.save(new UserFavorite(userId, curationId, type));
        }

    }
}
